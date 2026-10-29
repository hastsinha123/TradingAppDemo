package com.example.tradingappdemo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingappdemo.domain.model.Stock
import com.example.tradingappdemo.domain.model.stocks
import com.example.tradingappdemo.domain.usecase.ObserveLatestPriceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class TradingAppViewModel @Inject constructor(
    observeLatestPriceUseCase: ObserveLatestPriceUseCase
) : ViewModel() {

    private val stocksList: List<Stock> = stocks
    private val initial = TradingUiState(
        rows = stocksList.map {
            StockUi(it.identifier, it.name)
        },
        isConnecting = true
    )

    val uiState: StateFlow<TradingUiState> =
        observeLatestPriceUseCase.getStocksFlow(stocksList).sample(200.milliseconds)
            .runningFold(initial)
            { old, new ->
                old.reduce(new)
            }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000.milliseconds),
            initial
        )

    fun TradingUiState.reduce(prices: Map<String, Double>): TradingUiState {
        val newRows = rows.map {
            val newPrice = prices[it.symbol]
            if (newPrice == null || newPrice == it.price) {
                return@map it
            }

            val old = it.price
            it.copy(
                price = newPrice,
                priceText = formatPrice(newPrice),
                direction = when {
                    old == null -> Direction.NONE
                    old < newPrice -> Direction.UP
                    else -> Direction.DOWN
                },
                flashId = it.flashId + 1
            )
        }
        return copy(rows = newRows, isConnecting = newRows.all { it.price == null })
    }

    private fun formatPrice(p: Double): String = when {
        p >= 1000 -> String.format(Locale.US, "%,.2f", p)
        p >= 1 -> String.format(Locale.US, "%.3f", p)
        else -> String.format(Locale.US, "%.5f", p)
    }


}