package com.example.tradingappdemo.domain.repository

import com.example.tradingappdemo.domain.model.PriceData
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    fun observeData(identifiers: List<String>): Flow<PriceData>
}