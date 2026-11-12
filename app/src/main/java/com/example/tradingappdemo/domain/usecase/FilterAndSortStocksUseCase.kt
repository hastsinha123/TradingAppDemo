package com.example.tradingappdemo.domain.usecase

import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.StockFilter
import com.example.tradingappdemo.domain.model.StockQuote
import com.example.tradingappdemo.domain.model.StockSort
import javax.inject.Inject

class FilterAndSortStocksUseCase @Inject constructor() {

    operator fun invoke(
        items: List<StockQuote>,
        query: String,
        filter: StockFilter,
        sort: StockSort,
    ): List<StockQuote> {
        val needle = query.trim()
        val filtered = items.filter { matchesQuery(it, needle) && matchesFilter(it, filter) }

        return when (sort) {
            StockSort.WATCHLIST -> filtered
            StockSort.NAME -> filtered.sortedBy { it.stock.name.lowercase() }
            StockSort.PRICE -> filtered.sortedWith(
                compareBy<StockQuote> { it.price == null }.thenByDescending { it.price }
            )

            StockSort.CHANGE -> filtered.sortedWith(
                compareBy<StockQuote> { it.changePercent == null }.thenByDescending { it.changePercent }
            )
        }
    }

    private fun matchesQuery(item: StockQuote, needle: String): Boolean =
        needle.isEmpty() ||
                item.stock.name.contains(needle, ignoreCase = true) ||
                item.stock.ticker.contains(needle, ignoreCase = true)

    private fun matchesFilter(item: StockQuote, filter: StockFilter): Boolean = when (filter) {
        StockFilter.ALL -> true
        StockFilter.GAINERS -> item.changeDirection == Direction.UP
        StockFilter.LOSERS -> item.changeDirection == Direction.DOWN
    }
}
