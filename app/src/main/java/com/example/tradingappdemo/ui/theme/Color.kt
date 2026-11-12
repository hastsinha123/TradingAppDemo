package com.example.tradingappdemo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TradingColors(
    val gain: Color,
    val gainContainer: Color,
    val loss: Color,
    val lossContainer: Color,
    val warning: Color,
)

val LightTradingColors = TradingColors(
    gain = Color(0xFF15803D),
    gainContainer = Color(0xFFDCFCE7),
    loss = Color(0xFFB91C1C),
    lossContainer = Color(0xFFFEE2E2),
    warning = Color(0xFFB45309),
)

val DarkTradingColors = TradingColors(
    gain = Color(0xFF4ADE80),
    gainContainer = Color(0xFF12301F),
    loss = Color(0xFFF87171),
    lossContainer = Color(0xFF3A1618),
    warning = Color(0xFFFBBF24),
)

val LocalTradingColors = staticCompositionLocalOf { LightTradingColors }
