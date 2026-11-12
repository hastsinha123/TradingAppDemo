package com.example.tradingappdemo.domain.model

private const val FLAT_BAND_PERCENT = 0.005

data class StockQuote(val stock: Stock, val quote: Quote?) {

    val price: Double? get() = quote?.price

    val changePercent: Double? get() = quote?.changePercent

    val changeDirection: Direction
        get() {
            val percent = changePercent ?: return Direction.NONE
            return when {
                percent >= FLAT_BAND_PERCENT -> Direction.UP
                percent <= -FLAT_BAND_PERCENT -> Direction.DOWN
                else -> Direction.NONE
            }
        }
}
