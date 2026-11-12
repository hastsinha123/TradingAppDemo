package com.example.tradingappdemo.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.MarketSnapshot
import com.example.tradingappdemo.domain.model.Stock
import com.example.tradingappdemo.domain.model.StockFilter
import com.example.tradingappdemo.domain.model.StockQuote
import com.example.tradingappdemo.domain.model.StockSort
import com.example.tradingappdemo.domain.repository.WatchlistRepository
import com.example.tradingappdemo.domain.usecase.FilterAndSortStocksUseCase
import com.example.tradingappdemo.domain.usecase.ObserveMarketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class TradingAppViewModel @Inject constructor(
    private val observeMarket: ObserveMarketUseCase,
    private val filterAndSort: FilterAndSortStocksUseCase,
    watchlistRepository: WatchlistRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val watchlist: List<Stock> = watchlistRepository.getWatchlist()
    private val symbols: List<String> = watchlist.map { it.symbol }
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")
    private val filter = MutableStateFlow(StockFilter.ALL)
    private val sort = MutableStateFlow(StockSort.WATCHLIST)
    private val retryRequests = MutableStateFlow(0)

    private var lastSnapshot = MarketSnapshot()

    private val market: Flow<MarketSnapshot> = retryRequests
        .flatMapLatest {
            observeMarket(symbols, seed = lastSnapshot.copy(status = ConnectionStatus.Connecting))
        }
        .onEach { lastSnapshot = it }
        .sample(UI_SAMPLE_PERIOD_MS)

    val uiState: StateFlow<TradingUiState> =
        combine(market, query, filter, sort) { snapshot, text, selectedFilter, selectedSort ->
            buildState(snapshot, text, selectedFilter, selectedSort)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = TradingUiState(),
        )

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun onFilterChange(value: StockFilter) {
        filter.value = value
    }

    fun onSortChange(value: StockSort) {
        sort.value = value
    }

    fun onRetry() {
        retryRequests.update { it + 1 }
    }

    private fun buildState(
        snapshot: MarketSnapshot,
        query: String,
        filter: StockFilter,
        sort: StockSort,
    ): TradingUiState {
        val items = watchlist.map { StockQuote(it, snapshot.quotes[it.symbol]) }
        val isStale = snapshot.status != ConnectionStatus.Connected

        return TradingUiState(
            screen = resolveScreen(snapshot),
            rows = filterAndSort(items, query, filter, sort).map { it.toUi(isStale) },
            connection = snapshot.status,
            filter = filter,
            sort = sort,
            gainers = items.count { it.changeDirection == Direction.UP },
            losers = items.count { it.changeDirection == Direction.DOWN },
        )
    }

    private fun resolveScreen(snapshot: MarketSnapshot): ScreenState {
        if (snapshot.hasPrices) return ScreenState.Content
        return when (val status = snapshot.status) {
            ConnectionStatus.Offline -> ScreenState.Failed(ErrorKind.OFFLINE)
            is ConnectionStatus.Reconnecting ->
                if (status.attempt >= FAILURE_THRESHOLD) ScreenState.Failed(ErrorKind.CONNECTION_FAILED)
                else ScreenState.Loading

            ConnectionStatus.Connecting, ConnectionStatus.Connected -> ScreenState.Loading
        }
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val FAILURE_THRESHOLD = 3
        const val UI_SAMPLE_PERIOD_MS = 200L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
