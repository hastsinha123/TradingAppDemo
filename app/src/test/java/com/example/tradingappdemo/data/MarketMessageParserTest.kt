package com.example.tradingappdemo.data

import com.example.tradingappdemo.data.network.MarketMessageParser
import com.example.tradingappdemo.domain.model.MarketEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarketMessageParserTest {

    private val parser = MarketMessageParser()

    @Test
    fun `parses a trade frame`() {
        val json = """
            {"stream":"btcusdt@trade","data":{"e":"trade","E":1,"s":"BTCUSDT","t":12,
            "p":"67123.45","q":"0.01","T":1,"m":true,"M":true}}
        """.trimIndent()

        assertEquals(MarketEvent.Trade("BTCUSDT", 67123.45), parser.parse(json))
    }

    @Test
    fun `parses a 24h ticker frame`() {
        val json = """
            {"stream":"ethusdt@ticker","data":{"e":"24hrTicker","E":1,"s":"ETHUSDT",
            "p":"10","P":"0.3","c":"3400.50","o":"3390.50","h":"3500","l":"3300"}}
        """.trimIndent()

        assertEquals(MarketEvent.Stats24h("ETHUSDT", lastPrice = 3400.5, openPrice = 3390.5), parser.parse(json))
    }

    @Test
    fun `returns null for text that is not JSON`() {
        assertNull(parser.parse("not json"))
        assertNull(parser.parse(""))
    }

    @Test
    fun `returns null for JSON that is not an object`() {
        assertNull(parser.parse("[1,2,3]"))
        assertNull(parser.parse("42"))
        assertNull(parser.parse("null"))
    }

    @Test
    fun `returns null for frames without a data payload`() {
        assertNull(parser.parse("""{"result":null,"id":1}"""))
        assertNull(parser.parse("""{"stream":"x","data":"oops"}"""))
    }

    @Test
    fun `returns null for event types it does not handle`() {
        assertNull(parser.parse("""{"data":{"e":"kline","s":"BTCUSDT","p":"1"}}"""))
    }

    @Test
    fun `returns null when the symbol or price is missing`() {
        assertNull(parser.parse("""{"data":{"e":"trade","p":"1"}}"""))
        assertNull(parser.parse("""{"data":{"e":"trade","s":"BTCUSDT"}}"""))
        assertNull(parser.parse("""{"data":{"e":"24hrTicker","s":"BTCUSDT","c":"1"}}"""))
    }

    @Test
    fun `rejects prices that are not positive finite numbers`() {
        assertNull(parser.parse("""{"data":{"e":"trade","s":"BTCUSDT","p":"abc"}}"""))
        assertNull(parser.parse("""{"data":{"e":"trade","s":"BTCUSDT","p":"0"}}"""))
        assertNull(parser.parse("""{"data":{"e":"trade","s":"BTCUSDT","p":"-5"}}"""))
        assertNull(parser.parse("""{"data":{"e":"trade","s":"BTCUSDT","p":"NaN"}}"""))
        assertNull(parser.parse("""{"data":{"e":"trade","s":"BTCUSDT","p":"Infinity"}}"""))
    }
}
