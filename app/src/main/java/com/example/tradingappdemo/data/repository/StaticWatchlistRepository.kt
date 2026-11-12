package com.example.tradingappdemo.data.repository

import com.example.tradingappdemo.domain.model.Stock
import com.example.tradingappdemo.domain.repository.WatchlistRepository
import javax.inject.Inject

class StaticWatchlistRepository @Inject constructor() : WatchlistRepository {

    override fun getWatchlist(): List<Stock> = DEFAULT_WATCHLIST

    private companion object {
        val DEFAULT_WATCHLIST = listOf(
            Stock("BTCUSDT", "BTC", "Bitcoin"),
            Stock("ETHUSDT", "ETH", "Ethereum"),
            Stock("BNBUSDT", "BNB", "BNB"),
            Stock("SOLUSDT", "SOL", "Solana"),
            Stock("XRPUSDT", "XRP", "XRP"),
            Stock("DOGEUSDT", "DOGE", "Dogecoin"),
            Stock("ADAUSDT", "ADA", "Cardano"),
            Stock("TRXUSDT", "TRX", "TRON"),
            Stock("AVAXUSDT", "AVAX", "Avalanche"),
            Stock("LINKUSDT", "LINK", "Chainlink"),
            Stock("DOTUSDT", "DOT", "Polkadot"),
            Stock("LTCUSDT", "LTC", "Litecoin"),
            Stock("BCHUSDT", "BCH", "Bitcoin Cash"),
            Stock("ATOMUSDT", "ATOM", "Cosmos"),
            Stock("UNIUSDT", "UNI", "Uniswap"),
            Stock("SHIBUSDT", "SHIB", "Shiba Inu"),
            Stock("NEARUSDT", "NEAR", "NEAR Protocol"),
            Stock("AAVEUSDT", "AAVE", "Aave"),
            Stock("INJUSDT", "INJ", "Injective"),
            Stock("PEPEUSDT", "PEPE", "Pepe"),
            Stock("SUIUSDT", "SUI", "Sui"),
            Stock("ARBUSDT", "ARB", "Arbitrum"),
        )
    }
}
