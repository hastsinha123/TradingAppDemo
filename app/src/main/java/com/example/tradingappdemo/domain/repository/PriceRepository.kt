package com.example.tradingappdemo.domain.repository

import com.example.tradingappdemo.domain.model.MarketEvent
import kotlinx.coroutines.flow.Flow

interface PriceRepository {

    fun observeMarket(symbols: List<String>): Flow<MarketEvent>
}
