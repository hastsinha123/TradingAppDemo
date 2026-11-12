package com.example.tradingappdemo.domain.model

sealed interface MarketEvent {
    data class Status(val status: ConnectionStatus) : MarketEvent
    data class Trade(val symbol: String, val price: Double) : MarketEvent
    data class Stats24h(val symbol: String, val lastPrice: Double, val openPrice: Double) : MarketEvent
}
