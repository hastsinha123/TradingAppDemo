package com.example.tradingappdemo.data

import com.example.tradingappdemo.data.network.SocketEvent
import com.example.tradingappdemo.data.repository.PriceRepositoryImpl
import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.MarketEvent
import com.example.tradingappdemo.testutil.FakeMarketDataSource
import com.example.tradingappdemo.testutil.FakeNetworkMonitor
import com.example.tradingappdemo.testutil.FixedBackoffPolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PriceRepositoryImplTest {

    private val symbols = listOf("BTCUSDT")
    private val trade = MarketEvent.Trade("BTCUSDT", 100.0)

    private val connecting = MarketEvent.Status(ConnectionStatus.Connecting)
    private val connected = MarketEvent.Status(ConnectionStatus.Connected)
    private val offline = MarketEvent.Status(ConnectionStatus.Offline)
    private fun reconnecting(attempt: Int) = MarketEvent.Status(ConnectionStatus.Reconnecting(attempt))

    private fun healthySocket(vararg events: MarketEvent): Flow<SocketEvent> = flow {
        emit(SocketEvent.Opened)
        events.forEach { emit(SocketEvent.Received(it)) }
        awaitCancellation()
    }

    private fun failingSocket(): Flow<SocketEvent> = flow { throw IOException("boom") }

    private fun TestScope.repository(
        dataSource: FakeMarketDataSource,
        network: FakeNetworkMonitor = FakeNetworkMonitor(),
    ) = PriceRepositoryImpl(
        dataSource = dataSource,
        networkMonitor = network,
        backoffPolicy = FixedBackoffPolicy(1_000),
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    private fun TestScope.collectEvents(repository: PriceRepositoryImpl): List<MarketEvent> {
        val events = mutableListOf<MarketEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeMarket(symbols).toList(events)
        }
        return events
    }

    @Test
    fun `connects, reports status, and forwards market data`() = runTest {
        val events = collectEvents(repository(FakeMarketDataSource(listOf(healthySocket(trade)))))

        runCurrent()

        assertEquals(listOf(connecting, connected, trade), events)
    }

    @Test
    fun `retries after a failure once the backoff has elapsed`() = runTest {
        val dataSource = FakeMarketDataSource(listOf(failingSocket(), healthySocket(trade)))
        val events = collectEvents(repository(dataSource))

        runCurrent()
        assertEquals(listOf(connecting, reconnecting(1)), events)
        assertEquals(1, dataSource.connectCalls)

        advanceTimeBy(999)
        runCurrent()
        assertEquals("must not retry before the backoff elapses", 1, dataSource.connectCalls)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(2, dataSource.connectCalls)
        assertEquals(listOf(connecting, reconnecting(1), connected, trade), events)
    }

    @Test
    fun `a socket closed by the server is reconnected too`() = runTest {
        val closedByServer = flow<SocketEvent> { emit(SocketEvent.Opened) }
        val events = collectEvents(repository(FakeMarketDataSource(listOf(closedByServer, healthySocket()))))

        runCurrent()
        assertEquals(listOf(connecting, connected, reconnecting(1)), events)

        advanceTimeBy(1_001)
        runCurrent()
        assertEquals(connected, events.last())
    }

    @Test
    fun `consecutive failures are counted`() = runTest {
        val events = collectEvents(repository(FakeMarketDataSource(listOf(failingSocket()))))

        runCurrent()
        advanceTimeBy(1_001)
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(listOf(connecting, reconnecting(1), reconnecting(2), reconnecting(3)), events)
    }

    @Test
    fun `the failure count resets after a successful connection`() = runTest {
        val opensThenDrops = flow<SocketEvent> {
            emit(SocketEvent.Opened)
            throw IOException("dropped")
        }
        val events = collectEvents(repository(FakeMarketDataSource(listOf(opensThenDrops, failingSocket()))))

        runCurrent()
        advanceTimeBy(1_001)
        runCurrent()

        assertEquals(listOf(connecting, connected, reconnecting(1), reconnecting(2)), events)
    }

    @Test
    fun `stays offline without opening a socket, and connects when the network returns`() = runTest {
        val network = FakeNetworkMonitor(initiallyOnline = false)
        val dataSource = FakeMarketDataSource(listOf(healthySocket(trade)))
        val events = collectEvents(repository(dataSource, network))

        runCurrent()
        assertEquals(listOf(offline), events)
        assertEquals(0, dataSource.connectCalls)

        network.online.value = true
        runCurrent()
        assertEquals(listOf(offline, connecting, connected, trade), events)
        assertEquals(1, dataSource.connectCalls)
    }

    @Test
    fun `losing the network closes the connection and reports offline`() = runTest {
        val network = FakeNetworkMonitor(initiallyOnline = true)
        val events = collectEvents(repository(FakeMarketDataSource(listOf(healthySocket())), network))
        runCurrent()
        assertEquals(connected, events.last())

        network.online.value = false
        runCurrent()

        assertEquals(offline, events.last())
    }

    @Test
    fun `coming back online skips a long backoff and reconnects immediately`() = runTest {
        val network = FakeNetworkMonitor(initiallyOnline = true)
        val dataSource = FakeMarketDataSource(listOf(failingSocket(), healthySocket()))
        val events = collectEvents(repository(dataSource, network))
        runCurrent()
        network.online.value = false
        runCurrent()
        network.online.value = true
        runCurrent()

        assertEquals(2, dataSource.connectCalls)
        assertEquals(connected, events.last())
    }
}
