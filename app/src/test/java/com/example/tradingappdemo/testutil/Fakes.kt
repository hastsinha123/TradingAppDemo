package com.example.tradingappdemo.testutil

import com.example.tradingappdemo.data.network.BackoffPolicy
import com.example.tradingappdemo.data.network.MarketDataSource
import com.example.tradingappdemo.data.network.SocketEvent
import com.example.tradingappdemo.domain.model.MarketEvent
import com.example.tradingappdemo.domain.model.Quote
import com.example.tradingappdemo.domain.model.Stock
import com.example.tradingappdemo.domain.model.StockQuote
import com.example.tradingappdemo.domain.repository.NetworkMonitor
import com.example.tradingappdemo.domain.repository.PriceRepository
import com.example.tradingappdemo.domain.repository.WatchlistRepository
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeMarketDataSource(private val attempts: List<Flow<SocketEvent>>) : MarketDataSource {
    var connectCalls = 0
        private set

    override fun connect(symbols: List<String>): Flow<SocketEvent> {
        val attempt = attempts.getOrElse(connectCalls) { attempts.last() }
        connectCalls++
        return attempt
    }
}

class FakeNetworkMonitor(initiallyOnline: Boolean = true) : NetworkMonitor {
    val online = MutableStateFlow(initiallyOnline)
    override val isOnline: Flow<Boolean> = online
}

class FixedBackoffPolicy(private val millis: Long = 1_000L) : BackoffPolicy {
    override fun delayMillis(attempt: Int): Long = millis
}


class FakePriceRepository(private vararg val scripts: List<MarketEvent>) : PriceRepository {
    var observeCalls = 0
        private set

    override fun observeMarket(symbols: List<String>): Flow<MarketEvent> {
        val script = scripts.getOrElse(observeCalls) { scripts.last() }
        observeCalls++
        return flow {
            script.forEach { emit(it) }
            awaitCancellation()
        }
    }
}

class FakeWatchlistRepository(private val stocks: List<Stock>) : WatchlistRepository {
    override fun getWatchlist(): List<Stock> = stocks
}

fun stockQuote(
    ticker: String,
    name: String,
    price: Double? = null,
    open: Double? = null,
): StockQuote {
    val symbol = "${ticker}USDT"
    return StockQuote(
        stock = Stock(symbol, ticker, name),
        quote = if (price == null && open == null) null
        else Quote(symbol = symbol, price = price, openPrice24h = open),
    )
}
