package com.example.tradingappdemo.presentation

import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.Quote
import com.example.tradingappdemo.domain.model.Stock
import com.example.tradingappdemo.domain.model.StockQuote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StockUiMapperTest {

    private val stock = Stock("BTCUSDT", "BTC", "Bitcoin")

    @Test
    fun `maps a live quote to display text and directions`() {
        val quote = Quote(
            symbol = "BTCUSDT",
            price = 110.0,
            previousPrice = 108.0,
            openPrice24h = 100.0,
            tick = 3,
        )

        val ui = StockQuote(stock, quote).toUi(isStale = false)

        assertEquals("BTC", ui.ticker)
        assertEquals("Bitcoin", ui.name)
        assertEquals("\$110.000", ui.priceText)
        assertEquals("+10.00%", ui.changeText)
        assertEquals(Direction.UP, ui.changeDirection)
        assertEquals(Direction.UP, ui.tickDirection)
        assertEquals(3L, ui.flashId)
        assertFalse(ui.isStale)
    }

    @Test
    fun `a coin with no data yet shows placeholders`() {
        val ui = StockQuote(stock, quote = null).toUi(isStale = false)

        assertEquals(StockUi.PRICE_PLACEHOLDER, ui.priceText)
        assertNull(ui.changeText)
        assertEquals(Direction.NONE, ui.changeDirection)
        assertEquals(Direction.NONE, ui.tickDirection)
    }

    @Test
    fun `the change direction follows the 24h change, not the last tick`() {
        val quote = Quote("BTCUSDT", price = 90.0, previousPrice = 89.0, openPrice24h = 100.0, tick = 1)

        val ui = StockQuote(stock, quote).toUi(isStale = false)

        assertEquals(Direction.DOWN, ui.changeDirection)
        assertEquals(Direction.UP, ui.tickDirection)
    }

    @Test
    fun `stale flag is passed through`() {
        assertTrue(StockQuote(stock, null).toUi(isStale = true).isStale)
    }
}
