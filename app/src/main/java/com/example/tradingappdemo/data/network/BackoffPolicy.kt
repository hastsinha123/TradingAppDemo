package com.example.tradingappdemo.data.network

import kotlin.random.Random

interface BackoffPolicy {
    fun delayMillis(attempt: Int): Long
}

class ExponentialBackoffPolicy(
    private val baseDelayMillis: Long = 1_000L,
    private val maxDelayMillis: Long = 30_000L,
    private val jitterFraction: Double = 0.2,
    private val random: Random = Random.Default,
) : BackoffPolicy {

    override fun delayMillis(attempt: Int): Long {
        val exponent = (attempt - 1).coerceIn(0, MAX_EXPONENT)
        val exponential = (baseDelayMillis shl exponent).coerceAtMost(maxDelayMillis)
        if (jitterFraction <= 0.0) return exponential

        val spread = exponential * jitterFraction
        val jittered = exponential + random.nextDouble(-spread, spread)
        return jittered.toLong().coerceIn(0L, maxDelayMillis)
    }

    private companion object {
        const val MAX_EXPONENT = 20
    }
}
