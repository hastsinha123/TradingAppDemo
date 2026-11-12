package com.example.tradingappdemo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEBEEF7),
    onSurfaceVariant = Color(0xFF5B6478),
    outline = Color(0xFFC7CCDA),
    outlineVariant = Color(0xFFDDE1EE),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondaryContainer = Color(0xFF2A2F5C),
    onSecondaryContainer = Color(0xFFE0E7FF),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111729),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1A2236),
    onSurfaceVariant = Color(0xFF9AA4BA),
    outline = Color(0xFF3A4562),
    outlineVariant = Color(0xFF2A3350),
    errorContainer = Color(0xFF3A1618),
    onErrorContainer = Color(0xFFFECACA),
)

@Composable
fun TradingAppDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val tradingColors = if (darkTheme) DarkTradingColors else LightTradingColors

    CompositionLocalProvider(LocalTradingColors provides tradingColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
