package com.example.tradingappdemo.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `prices of 1000 and above use thousands separators and 2 decimals`() {
        assertEquals("\$67,123.46", formatPrice(67123.456))
        assertEquals("\$1,000.00", formatPrice(1000.0))
    }

    @Test
    fun `prices between 1 and 1000 use 3 decimals`() {
        assertEquals("\$2.500", formatPrice(2.5))
        assertEquals("\$110.000", formatPrice(110.0))
    }

    @Test
    fun `prices below 1 use 5 decimals`() {
        assertEquals("\$0.12340", formatPrice(0.1234))
    }

    @Test
    fun `very small prices keep enough precision to be visible`() {
        assertEquals("\$0.00001234", formatPrice(0.00001234))
    }

    @Test
    fun `positive changes get a plus sign`() {
        assertEquals("+2.35%", formatChangePercent(2.3456))
        assertEquals("+12.00%", formatChangePercent(12.0))
    }

    @Test
    fun `negative changes keep their minus sign`() {
        assertEquals("-0.50%", formatChangePercent(-0.5))
    }

    @Test
    fun `changes that round to zero show no sign`() {
        assertEquals("0.00%", formatChangePercent(0.0))
        assertEquals("0.00%", formatChangePercent(0.004))
        assertEquals("0.00%", formatChangePercent(-0.004))
    }
}
