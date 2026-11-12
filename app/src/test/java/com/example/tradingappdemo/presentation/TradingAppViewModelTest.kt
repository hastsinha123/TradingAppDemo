package com.example.tradingappdemo.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.Direction
import com.example.tradingappdemo.domain.model.MarketEvent
import com.example.tradingappdemo.domain.model.Stock
import com.example.tradingappdemo.domain.model.StockFilter
import com.example.tradingappdemo.domain.model.StockSort
import com.example.tradingappdemo.domain.usecase.FilterAndSortStocksUseCase
import com.example.tradingappdemo.domain.usecase.ObserveMarketUseCase
import com.example.tradingappdemo.testutil.FakePriceRepository
import com.example.tradingappdemo.testutil.FakeWatchlistRepository
import com.example.tradingappdemo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TradingAppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val watchlist = listOf(
        Stock("BTCUSDT", "BTC", "Bitcoin"),
        Stock("ETHUSDT", "ETH", "Ethereum"),
        Stock("DOGEUSDT", "DOGE", "Dogecoin"),
    )

    private val connected = MarketEvent.Status(ConnectionStatus.Connected)

    private val marketData = listOf(
        connected,
        MarketEvent.Stats24h("BTCUSDT", lastPrice = 110.0, openPrice = 100.0),
        MarketEvent.Stats24h("ETHUSDT", lastPrice = 90.0, openPrice = 100.0),
    )

    private fun viewModel(repository: FakePriceRepository) = TradingAppViewModel(
        observeMarket = ObserveMarketUseCase(repository),
        filterAndSort = FilterAndSortStocksUseCase(),
        watchlistRepository = FakeWatchlistRepository(watchlist),
        savedStateHandle = SavedStateHandle(),
    )

    /** uiState is a WhileSubscribed StateFlow, so nothing happens until someone collects it. */
    private fun TestScope.subscribe(viewModel: TradingAppViewModel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

    /** Lets the 200 ms UI sampling window elapse. */
    private fun TestScope.letUiSettle() = advanceTimeBy(250)

    private val TradingAppViewModel.tickers get() = uiState.value.rows.map { it.ticker }

    @Test
    fun `starts in the loading state`() = runTest {
        val viewModel = viewModel(FakePriceRepository(marketData))

        assertEquals(ScreenState.Loading, viewModel.uiState.value.screen)
        assertTrue(viewModel.uiState.value.rows.isEmpty())
    }

    @Test
    fun `shows prices and 24h change once data arrives`() = runTest {
        val viewModel = viewModel(FakePriceRepository(marketData + MarketEvent.Trade("BTCUSDT", 120.0)))
        subscribe(viewModel)

        letUiSettle()

        val state = viewModel.uiState.value
        assertEquals(ScreenState.Content, state.screen)
        assertEquals(ConnectionStatus.Connected, state.connection)

        val btc = state.rows.first { it.ticker == "BTC" }
        assertEquals("\$120.000", btc.priceText)
        assertEquals("+20.00%", btc.changeText)
        assertEquals(Direction.UP, btc.changeDirection)
        assertEquals(false, btc.isStale)

        val doge = state.rows.first { it.ticker == "DOGE" }
        assertEquals(StockUi.PRICE_PLACEHOLDER, doge.priceText)
    }

    @Test
    fun `counts gainers and losers across the whole watchlist`() = runTest {
        val viewModel = viewModel(FakePriceRepository(marketData))
        subscribe(viewModel)

        letUiSettle()

        assertEquals(1, viewModel.uiState.value.gainers)
        assertEquals(1, viewModel.uiState.value.losers)
    }

    @Test
    fun `search filters the list immediately`() = runTest {
        val viewModel = viewModel(FakePriceRepository(marketData))
        subscribe(viewModel)
        letUiSettle()

        viewModel.onQueryChange("eth")
        runCurrent()

        assertEquals(listOf("ETH"), viewModel.tickers)
        assertEquals("eth", viewModel.query.value)
    }

    @Test
    fun `a search with no results gives an empty list but keeps showing content`() = runTest {
        val viewModel = viewModel(FakePriceRepository(marketData))
        subscribe(viewModel)
        letUiSettle()

        viewModel.onQueryChange("zzz")
        runCurrent()

        assertEquals(emptyList<String>(), viewModel.tickers)
        assertEquals(ScreenState.Content, viewModel.uiState.value.screen)
    }

    @Test
    fun `gainers and losers filters narrow the list`() = runTest {
        val viewModel = viewModel(FakePriceRepository(marketData))
        subscribe(viewModel)
        letUiSettle()

        viewModel.onFilterChange(StockFilter.GAINERS)
        runCurrent()
        assertEquals(listOf("BTC"), viewModel.tickers)

        viewModel.onFilterChange(StockFilter.LOSERS)
        runCurrent()
        assertEquals(listOf("ETH"), viewModel.tickers)
    }

    @Test
    fun `sorting by change orders by 24h move with unknowns last`() = runTest {
        val viewModel = viewModel(FakePriceRepository(marketData))
        subscribe(viewModel)
        letUiSettle()

        viewModel.onSortChange(StockSort.CHANGE)
        runCurrent()

        assertEquals(listOf("BTC", "ETH", "DOGE"), viewModel.tickers)
    }

    @Test
    fun `offline with no data shows the offline error`() = runTest {
        val viewModel = viewModel(FakePriceRepository(listOf(MarketEvent.Status(ConnectionStatus.Offline))))
        subscribe(viewModel)

        letUiSettle()

        assertEquals(ScreenState.Failed(ErrorKind.OFFLINE), viewModel.uiState.value.screen)
    }

    @Test
    fun `repeated connection failures with no data show the connection error`() = runTest {
        val failures = listOf(
            MarketEvent.Status(ConnectionStatus.Connecting),
            MarketEvent.Status(ConnectionStatus.Reconnecting(1)),
            MarketEvent.Status(ConnectionStatus.Reconnecting(2)),
            MarketEvent.Status(ConnectionStatus.Reconnecting(3)),
        )
        val viewModel = viewModel(FakePriceRepository(failures))
        subscribe(viewModel)

        letUiSettle()

        assertEquals(ScreenState.Failed(ErrorKind.CONNECTION_FAILED), viewModel.uiState.value.screen)
    }

    @Test
    fun `an early failed attempt is still just loading`() = runTest {
        val events = listOf(
            MarketEvent.Status(ConnectionStatus.Connecting),
            MarketEvent.Status(ConnectionStatus.Reconnecting(1)),
        )
        val viewModel = viewModel(FakePriceRepository(events))
        subscribe(viewModel)

        letUiSettle()

        assertEquals(ScreenState.Loading, viewModel.uiState.value.screen)
    }

    @Test
    fun `losing the connection with data keeps the rows and marks them stale`() = runTest {
        val viewModel = viewModel(
            FakePriceRepository(marketData + MarketEvent.Status(ConnectionStatus.Reconnecting(1)))
        )
        subscribe(viewModel)

        letUiSettle()

        val state = viewModel.uiState.value
        assertEquals(ScreenState.Content, state.screen)
        assertEquals(ConnectionStatus.Reconnecting(1), state.connection)
        assertTrue(state.rows.all { it.isStale })
        assertEquals("\$110.000", state.rows.first { it.ticker == "BTC" }.priceText)
    }

    @Test
    fun `retry restarts the connection and recovers from the error screen`() = runTest {
        val repository = FakePriceRepository(
            listOf(MarketEvent.Status(ConnectionStatus.Offline)),
            marketData,
        )
        val viewModel = viewModel(repository)
        subscribe(viewModel)
        letUiSettle()
        assertEquals(ScreenState.Failed(ErrorKind.OFFLINE), viewModel.uiState.value.screen)

        viewModel.onRetry()
        letUiSettle()

        assertEquals(2, repository.observeCalls)
        assertEquals(ScreenState.Content, viewModel.uiState.value.screen)
    }

    @Test
    fun `returning from the background keeps the last known prices instead of resetting`() = runTest {
        val repository = FakePriceRepository(
            marketData,
            listOf(MarketEvent.Status(ConnectionStatus.Connecting)),
        )
        val viewModel = viewModel(repository)

        val firstVisit = subscribe(viewModel)
        letUiSettle()
        assertEquals("\$110.000", viewModel.uiState.value.rows.first { it.ticker == "BTC" }.priceText)

        firstVisit.cancel()
        advanceTimeBy(6_000)

        subscribe(viewModel)
        letUiSettle()

        val state = viewModel.uiState.value
        assertEquals(2, repository.observeCalls)
        assertEquals(ScreenState.Content, state.screen)
        assertEquals("\$110.000", state.rows.first { it.ticker == "BTC" }.priceText)
        assertTrue("prices should be stale until reconnected", state.rows.all { it.isStale })
    }

    @Test
    fun `a brief interruption does not reopen the socket`() = runTest {
        val repository = FakePriceRepository(marketData)
        val viewModel = viewModel(repository)

        val firstVisit = subscribe(viewModel)
        letUiSettle()
        firstVisit.cancel()
        advanceTimeBy(2_000)
        subscribe(viewModel)
        letUiSettle()

        assertEquals(1, repository.observeCalls)
    }
}
