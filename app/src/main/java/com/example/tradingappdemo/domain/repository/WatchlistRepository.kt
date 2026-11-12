package com.example.tradingappdemo.domain.repository

import com.example.tradingappdemo.domain.model.Stock

interface WatchlistRepository {
    fun getWatchlist(): List<Stock>
}
