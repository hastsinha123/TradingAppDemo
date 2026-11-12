package com.example.tradingappdemo.di

import com.example.tradingappdemo.data.network.ConnectivityNetworkMonitor
import com.example.tradingappdemo.data.network.MarketDataSource
import com.example.tradingappdemo.data.network.WebSocketDataSource
import com.example.tradingappdemo.data.repository.PriceRepositoryImpl
import com.example.tradingappdemo.data.repository.StaticWatchlistRepository
import com.example.tradingappdemo.domain.repository.NetworkMonitor
import com.example.tradingappdemo.domain.repository.PriceRepository
import com.example.tradingappdemo.domain.repository.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPriceRepository(impl: PriceRepositoryImpl): PriceRepository

    @Binds
    abstract fun bindWatchlistRepository(impl: StaticWatchlistRepository): WatchlistRepository

    @Binds
    abstract fun bindMarketDataSource(impl: WebSocketDataSource): MarketDataSource

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
