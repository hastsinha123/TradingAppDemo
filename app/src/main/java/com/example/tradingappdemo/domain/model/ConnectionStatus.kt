package com.example.tradingappdemo.domain.model

sealed interface ConnectionStatus {
    data object Connecting : ConnectionStatus
    data object Connected : ConnectionStatus
    data class Reconnecting(val attempt: Int) : ConnectionStatus
    data object Offline : ConnectionStatus
}
