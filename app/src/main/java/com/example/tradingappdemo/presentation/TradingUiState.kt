package com.example.tradingappdemo.presentation

import androidx.compose.runtime.Immutable

enum class Direction { NONE, UP, DOWN }

@Immutable
data class StockUi(
    val symbol: String,
    val name: String,
    val price: Double? = null,
    val priceText: String = "--",
    val direction: Direction = Direction.NONE,
    val flashId: Long = 0L,
)

@Immutable
data class TradingUiState(
    val rows: List<StockUi> = emptyList(),
    val isConnecting: Boolean = true,
)