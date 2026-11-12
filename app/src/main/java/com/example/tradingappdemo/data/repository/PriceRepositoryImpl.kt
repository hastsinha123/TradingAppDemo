package com.example.tradingappdemo.data.repository

import com.example.tradingappdemo.data.network.BackoffPolicy
import com.example.tradingappdemo.data.network.MarketDataSource
import com.example.tradingappdemo.data.network.SocketEvent
import com.example.tradingappdemo.di.IoDispatcher
import com.example.tradingappdemo.domain.model.ConnectionStatus
import com.example.tradingappdemo.domain.model.MarketEvent
import com.example.tradingappdemo.domain.repository.NetworkMonitor
import com.example.tradingappdemo.domain.repository.PriceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class PriceRepositoryImpl @Inject constructor(
    private val dataSource: MarketDataSource,
    private val networkMonitor: NetworkMonitor,
    private val backoffPolicy: BackoffPolicy,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PriceRepository {

    override fun observeMarket(symbols: List<String>): Flow<MarketEvent> =
        networkMonitor.isOnline
            .distinctUntilChanged()
            .flatMapLatest { online ->
                if (online) connectWithRetry(symbols)
                else flowOf(MarketEvent.Status(ConnectionStatus.Offline))
            }
            .flowOn(ioDispatcher)

    private fun connectWithRetry(symbols: List<String>): Flow<MarketEvent> = flow {
        var consecutiveFailures = 0
        emit(MarketEvent.Status(ConnectionStatus.Connecting))

        while (true) {
            dataSource.connect(symbols)
                .catch { }
                .collect { event ->
                    when (event) {
                        SocketEvent.Opened -> {
                            consecutiveFailures = 0
                            emit(MarketEvent.Status(ConnectionStatus.Connected))
                        }

                        is SocketEvent.Received -> emit(event.event)
                    }
                }
            consecutiveFailures++
            emit(MarketEvent.Status(ConnectionStatus.Reconnecting(consecutiveFailures)))
            delay(backoffPolicy.delayMillis(consecutiveFailures))
        }
    }
}
