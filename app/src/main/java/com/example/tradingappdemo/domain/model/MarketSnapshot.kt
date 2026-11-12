package com.example.tradingappdemo.domain.model

data class MarketSnapshot(
    val quotes: Map<String, Quote> = emptyMap(),
    val status: ConnectionStatus = ConnectionStatus.Connecting,
) {
    val hasPrices: Boolean get() = quotes.values.any { it.price != null }

    fun applyEvent(event: MarketEvent): MarketSnapshot = when (event) {
        is MarketEvent.Status -> copy(status = event.status)
        is MarketEvent.Trade -> withQuote(event.symbol) { it.withTrade(event.price) }
        is MarketEvent.Stats24h -> withQuote(event.symbol) { it.withStats(event.lastPrice, event.openPrice) }
    }

    private fun withQuote(symbol: String, transform: (Quote) -> Quote): MarketSnapshot {
        val current = quotes[symbol]
        val updated = transform(current ?: Quote(symbol))
        return if (current != null && updated === current) this
        else copy(quotes = quotes + (symbol to updated))
    }
}
