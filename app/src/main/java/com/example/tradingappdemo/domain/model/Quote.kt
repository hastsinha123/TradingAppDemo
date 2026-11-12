package com.example.tradingappdemo.domain.model


data class Quote(
    val symbol: String,
    val price: Double? = null,
    val previousPrice: Double? = null,
    val openPrice24h: Double? = null,
    val tick: Long = 0L,
) {

    val tickDirection: Direction
        get() {
            val current = price ?: return Direction.NONE
            val previous = previousPrice ?: return Direction.NONE
            return when {
                current > previous -> Direction.UP
                current < previous -> Direction.DOWN
                else -> Direction.NONE
            }
        }

    val changePercent: Double?
        get() {
            val current = price ?: return null
            val open = openPrice24h ?: return null
            if (open == 0.0) return null
            return (current - open) / open * 100.0
        }

    fun withTrade(newPrice: Double): Quote =
        if (newPrice == price) this else copy(
            price = newPrice,
            previousPrice = price,
            tick = tick + 1
        )

    fun withStats(lastPrice: Double, openPrice: Double): Quote =
        copy(price = price ?: lastPrice, openPrice24h = openPrice)
}
