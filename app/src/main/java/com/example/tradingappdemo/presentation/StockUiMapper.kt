package com.example.tradingappdemo.presentation

import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.StockQuote

fun StockQuote.toUi(isStale: Boolean): StockUi {
    val liveQuote = quote
    return StockUi(
        symbol = stock.symbol,
        ticker = stock.ticker,
        name = stock.name,
        priceText = price?.let(::formatPrice) ?: StockUi.PRICE_PLACEHOLDER,
        changeText = changePercent?.let(::formatChangePercent),
        changeDirection = changeDirection,
        tickDirection = liveQuote?.tickDirection ?: Direction.NONE,
        flashId = liveQuote?.tick ?: 0L,
        isStale = isStale,
    )
}
