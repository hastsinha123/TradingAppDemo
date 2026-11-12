package com.example.tradingappdemo.domain

import com.example.tradingappdemo.domain.model.StockFilter
import com.example.tradingappdemo.domain.model.StockQuote
import com.example.tradingappdemo.domain.model.StockSort
import com.example.tradingappdemo.domain.usecase.FilterAndSortStocksUseCase
import com.example.tradingappdemo.testutil.stockQuote
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterAndSortStocksUseCaseTest {

    private val useCase = FilterAndSortStocksUseCase()

    private val btc = stockQuote("BTC", "Bitcoin", price = 105.0, open = 100.0)
    private val eth = stockQuote("ETH", "Ethereum", price = 98.0, open = 100.0)
    private val doge = stockQuote("DOGE", "Dogecoin", price = 0.11, open = 0.10)
    private val ada = stockQuote("ADA", "Cardano")
    private val all = listOf(btc, eth, doge, ada)

    private fun run(
        query: String = "",
        filter: StockFilter = StockFilter.ALL,
        sort: StockSort = StockSort.WATCHLIST,
    ): List<String> = useCase(all, query, filter, sort).tickers()

    private fun List<StockQuote>.tickers() = map { it.stock.ticker }

    @Test
    fun `no query, filter or sort keeps the watchlist order`() {
        assertEquals(listOf("BTC", "ETH", "DOGE", "ADA"), run())
    }

    @Test
    fun `search matches the coin name ignoring case`() {
        assertEquals(listOf("BTC"), run(query = "bitco"))
    }

    @Test
    fun `search matches the ticker ignoring case`() {
        assertEquals(listOf("ETH"), run(query = "eth"))
    }

    @Test
    fun `search ignores surrounding whitespace`() {
        assertEquals(listOf("DOGE"), run(query = "  doge "))
    }

    @Test
    fun `blank search returns everything`() {
        assertEquals(4, run(query = "   ").size)
    }

    @Test
    fun `search with no match returns an empty list`() {
        assertEquals(emptyList<String>(), run(query = "zzz"))
    }

    @Test
    fun `gainers filter keeps only coins that are up`() {
        assertEquals(listOf("BTC", "DOGE"), run(filter = StockFilter.GAINERS))
    }

    @Test
    fun `losers filter keeps only coins that are down`() {
        assertEquals(listOf("ETH"), run(filter = StockFilter.LOSERS))
    }

    @Test
    fun `coins without data are neither gainers nor losers`() {
        val gainers = run(filter = StockFilter.GAINERS)
        val losers = run(filter = StockFilter.LOSERS)

        assertEquals(false, "ADA" in gainers)
        assertEquals(false, "ADA" in losers)
    }

    @Test
    fun `a flat coin is neither a gainer nor a loser`() {
        val flat = stockQuote("XRP", "XRP", price = 100.001, open = 100.0)
        val result = useCase(listOf(flat), "", StockFilter.GAINERS, StockSort.WATCHLIST)

        assertEquals(emptyList<StockQuote>(), result)
    }

    @Test
    fun `sorting by change puts the biggest gainer first and unknowns last`() {
        assertEquals(listOf("DOGE", "BTC", "ETH", "ADA"), run(sort = StockSort.CHANGE))
    }

    @Test
    fun `sorting by price puts the most expensive first and unknowns last`() {
        assertEquals(listOf("BTC", "ETH", "DOGE", "ADA"), run(sort = StockSort.PRICE))
    }

    @Test
    fun `sorting by name is alphabetical`() {
        assertEquals(listOf("BTC", "ADA", "DOGE", "ETH").sortedBy { name(it) }, run(sort = StockSort.NAME))
    }

    @Test
    fun `search, filter and sort combine`() {
        assertEquals(
            listOf("DOGE", "BTC"),
            run(query = "o", filter = StockFilter.GAINERS, sort = StockSort.CHANGE),
        )
    }

    private fun name(ticker: String) = all.first { it.stock.ticker == ticker }.stock.name
}
