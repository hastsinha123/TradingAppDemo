package com.example.tradingappdemo.data.network

import com.example.tradingappdemo.domain.model.MarketEvent
import kotlinx.coroutines.flow.Flow

sealed interface SocketEvent {
    data object Opened : SocketEvent

    data class Received(val event: MarketEvent) : SocketEvent
}

interface MarketDataSource {

    fun connect(symbols: List<String>): Flow<SocketEvent>
}
