package com.example.tradingappdemo.presentation

import androidx.compose.runtime.Immutable
import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.StockFilter
import com.example.tradingappdemo.domain.model.StockSort

@Immutable
data class StockUi(
    val symbol: String,
    val ticker: String,
    val name: String,
    val priceText: String = PRICE_PLACEHOLDER,
    val changeText: String? = null,
    val changeDirection: Direction = Direction.NONE,
    val tickDirection: Direction = Direction.NONE,
    val flashId: Long = 0L,
    val isStale: Boolean = false,
) {
    companion object {
        const val PRICE_PLACEHOLDER = "--"
    }
}

enum class ErrorKind { OFFLINE, CONNECTION_FAILED }

sealed interface ScreenState {
    data object Loading : ScreenState

    data object Content : ScreenState

    data class Failed(val kind: ErrorKind) : ScreenState
}

@Immutable
data class TradingUiState(
    val screen: ScreenState = ScreenState.Loading,
    val rows: List<StockUi> = emptyList(),
    val connection: ConnectionStatus = ConnectionStatus.Connecting,
    val filter: StockFilter = StockFilter.ALL,
    val sort: StockSort = StockSort.WATCHLIST,
    val gainers: Int = 0,
    val losers: Int = 0,
)
