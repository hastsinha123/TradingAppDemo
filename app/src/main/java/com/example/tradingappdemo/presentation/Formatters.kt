package com.example.tradingappdemo.presentation

import java.util.Locale

fun formatPrice(price: Double): String {
    val number = when {
        price >= 1_000 -> String.format(Locale.US, "%,.2f", price)
        price >= 1 -> String.format(Locale.US, "%.3f", price)
        price >= 0.01 -> String.format(Locale.US, "%.5f", price)
        else -> String.format(Locale.US, "%.8f", price)
    }
    return "\$$number"
}

fun formatChangePercent(percent: Double): String {
    val text = String.format(Locale.US, "%+.2f%%", percent)
    return if (text == "+0.00%" || text == "-0.00%") "0.00%" else text
}
