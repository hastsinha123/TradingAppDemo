package com.example.tradingappdemo.domain.model

data class Stock(
    val identifier : String,
    val name: String
)

val stocks = listOf(
    Stock("BTCUSDT", "Bitcoin"),
    Stock("ETHUSDT", "Ethereum"),
    Stock("BNBUSDT", "BNB"),
    Stock("SOLUSDT", "Solana"),
    Stock("XRPUSDT", "XRP"),
    Stock("DOGEUSDT", "Dogecoin"),
    Stock("ADAUSDT", "Cardano"),
    Stock("TRXUSDT", "TRON"),
)