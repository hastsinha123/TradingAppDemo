package com.example.tradingappdemo.domain

import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.MarketEvent
import com.example.tradingappdemo.domain.model.MarketSnapshot
import com.example.tradingappdemo.domain.model.Quote
import com.example.tradingappdemo.domain.repository.PriceRepository
import com.example.tradingappdemo.domain.usecase.ObserveMarketUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveMarketUseCaseTest {

    private fun useCase(vararg events: MarketEvent) = ObserveMarketUseCase(
        object : PriceRepository {
            override fun observeMarket(symbols: List<String>): Flow<MarketEvent> = flowOf(*events)
        }
    )

    @Test
    fun `emits the seed first, then one snapshot per event`() = runTest {
        val snapshots = useCase(
            MarketEvent.Status(ConnectionStatus.Connected),
            MarketEvent.Trade("BTCUSDT", 100.0),
        )(listOf("BTCUSDT")).toList()

        assertEquals(3, snapshots.size)
        assertEquals(MarketSnapshot(), snapshots[0])
        assertEquals(ConnectionStatus.Connected, snapshots[1].status)
        assertEquals(100.0, snapshots[2].quotes.getValue("BTCUSDT").price)
    }

    @Test
    fun `a seed snapshot keeps its quotes while the stream restarts`() = runTest {
        val seed = MarketSnapshot(quotes = mapOf("BTCUSDT" to Quote("BTCUSDT", price = 50.0)))

        val snapshots = useCase(MarketEvent.Status(ConnectionStatus.Connecting))(listOf("BTCUSDT"), seed).toList()

        assertEquals(50.0, snapshots.last().quotes.getValue("BTCUSDT").price)
        assertEquals(ConnectionStatus.Connecting, snapshots.last().status)
    }
}
