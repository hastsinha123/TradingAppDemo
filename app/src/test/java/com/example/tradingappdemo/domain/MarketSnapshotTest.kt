package com.example.tradingappdemo.domain

import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.MarketEvent
import com.example.tradingappdemo.domain.model.MarketSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketSnapshotTest {

    private val empty = MarketSnapshot()

    @Test
    fun `first trade creates a quote without a direction`() {
        val quote = empty.applyEvent(MarketEvent.Trade("BTCUSDT", 100.0)).quotes.getValue("BTCUSDT")

        assertEquals(100.0, quote.price)
        assertEquals(Direction.NONE, quote.tickDirection)
    }

    @Test
    fun `a higher trade price ticks up and a lower one ticks down`() {
        val up = empty
            .applyEvent(MarketEvent.Trade("BTCUSDT", 100.0))
            .applyEvent(MarketEvent.Trade("BTCUSDT", 101.0))
            .quotes.getValue("BTCUSDT")
        assertEquals(Direction.UP, up.tickDirection)

        val down = empty
            .applyEvent(MarketEvent.Trade("BTCUSDT", 100.0))
            .applyEvent(MarketEvent.Trade("BTCUSDT", 99.0))
            .quotes.getValue("BTCUSDT")
        assertEquals(Direction.DOWN, down.tickDirection)
    }

    @Test
    fun `tick counter increases only when the price actually changes`() {
        val afterFirst = empty.applyEvent(MarketEvent.Trade("BTCUSDT", 100.0))
        val afterSame = afterFirst.applyEvent(MarketEvent.Trade("BTCUSDT", 100.0))
        val afterChange = afterSame.applyEvent(MarketEvent.Trade("BTCUSDT", 101.0))

        assertEquals(1L, afterFirst.quotes.getValue("BTCUSDT").tick)
        assertEquals(1L, afterSame.quotes.getValue("BTCUSDT").tick)
        assertEquals(2L, afterChange.quotes.getValue("BTCUSDT").tick)
    }

    @Test
    fun `a repeated price returns the very same snapshot instance`() {
        val snapshot = empty.applyEvent(MarketEvent.Trade("BTCUSDT", 100.0))

        assertSame(snapshot, snapshot.applyEvent(MarketEvent.Trade("BTCUSDT", 100.0)))
    }

    @Test
    fun `24h stats provide the reference for percent change`() {
        val quote = empty
            .applyEvent(MarketEvent.Stats24h("BTCUSDT", lastPrice = 100.0, openPrice = 100.0))
            .applyEvent(MarketEvent.Trade("BTCUSDT", 110.0))
            .quotes.getValue("BTCUSDT")

        assertEquals(10.0, quote.changePercent!!, 1e-9)
    }

    @Test
    fun `percent change is negative when the price is below the 24h open`() {
        val quote = empty
            .applyEvent(MarketEvent.Stats24h("ETHUSDT", lastPrice = 90.0, openPrice = 100.0))
            .quotes.getValue("ETHUSDT")

        assertEquals(-10.0, quote.changePercent!!, 1e-9)
    }

    @Test
    fun `stats supply the price only until the first trade arrives`() {
        val fromStats = empty.applyEvent(MarketEvent.Stats24h("BTCUSDT", 100.0, 95.0))
        assertEquals(100.0, fromStats.quotes.getValue("BTCUSDT").price)

        val afterTrade = fromStats
            .applyEvent(MarketEvent.Trade("BTCUSDT", 105.0))
            .applyEvent(MarketEvent.Stats24h("BTCUSDT", 100.0, 95.0))
        assertEquals(105.0, afterTrade.quotes.getValue("BTCUSDT").price)
    }

    @Test
    fun `percent change is unknown until both price and open are known`() {
        val onlyTrade = empty.applyEvent(MarketEvent.Trade("BTCUSDT", 100.0)).quotes.getValue("BTCUSDT")
        assertNull(onlyTrade.changePercent)
    }

    @Test
    fun `a zero open price never causes a division by zero`() {
        val quote = empty
            .applyEvent(MarketEvent.Stats24h("BTCUSDT", lastPrice = 1.0, openPrice = 0.0))
            .quotes.getValue("BTCUSDT")

        assertNull(quote.changePercent)
    }

    @Test
    fun `status events update the status and keep existing quotes`() {
        val snapshot = empty
            .applyEvent(MarketEvent.Trade("BTCUSDT", 100.0))
            .applyEvent(MarketEvent.Status(ConnectionStatus.Reconnecting(2)))

        assertEquals(ConnectionStatus.Reconnecting(2), snapshot.status)
        assertEquals(100.0, snapshot.quotes.getValue("BTCUSDT").price)
    }

    @Test
    fun `hasPrices is false until a price has arrived`() {
        assertFalse(empty.hasPrices)
        assertFalse(empty.applyEvent(MarketEvent.Status(ConnectionStatus.Connected)).hasPrices)
        assertTrue(empty.applyEvent(MarketEvent.Trade("BTCUSDT", 1.0)).hasPrices)
    }

    @Test
    fun `symbols are tracked independently`() {
        val snapshot = empty
            .applyEvent(MarketEvent.Trade("BTCUSDT", 100.0))
            .applyEvent(MarketEvent.Trade("ETHUSDT", 5.0))

        assertEquals(100.0, snapshot.quotes.getValue("BTCUSDT").price)
        assertEquals(5.0, snapshot.quotes.getValue("ETHUSDT").price)
    }
}
