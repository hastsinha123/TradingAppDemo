package com.example.tradingappdemo.data

import com.example.tradingappdemo.data.network.ExponentialBackoffPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ExponentialBackoffPolicyTest {

    private val noJitter = ExponentialBackoffPolicy(
        baseDelayMillis = 1_000,
        maxDelayMillis = 30_000,
        jitterFraction = 0.0,
    )

    @Test
    fun `delay doubles with every attempt`() {
        assertEquals(1_000L, noJitter.delayMillis(1))
        assertEquals(2_000L, noJitter.delayMillis(2))
        assertEquals(4_000L, noJitter.delayMillis(3))
        assertEquals(8_000L, noJitter.delayMillis(4))
        assertEquals(16_000L, noJitter.delayMillis(5))
    }

    @Test
    fun `delay is capped at the maximum`() {
        assertEquals(30_000L, noJitter.delayMillis(6))
        assertEquals(30_000L, noJitter.delayMillis(50))
        assertEquals(30_000L, noJitter.delayMillis(Int.MAX_VALUE))
    }

    @Test
    fun `attempts below one fall back to the base delay`() {
        assertEquals(1_000L, noJitter.delayMillis(0))
        assertEquals(1_000L, noJitter.delayMillis(-3))
    }

    @Test
    fun `jitter stays within the configured fraction and never exceeds the cap`() {
        val policy = ExponentialBackoffPolicy(
            baseDelayMillis = 1_000,
            maxDelayMillis = 30_000,
            jitterFraction = 0.2,
            random = Random(seed = 42),
        )

        repeat(200) {
            val third = policy.delayMillis(3)
            assertTrue("delay $third out of range", third in 3_200L..4_800L)

            val capped = policy.delayMillis(10)
            assertTrue("delay $capped out of range", capped in 24_000L..30_000L)
        }
    }
}
