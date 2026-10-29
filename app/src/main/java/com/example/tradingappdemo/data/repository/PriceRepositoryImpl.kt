package com.example.tradingappdemo.data.repository

import com.example.tradingappdemo.data.network.WebSocketDataSource
import com.example.tradingappdemo.domain.model.PriceData
import com.example.tradingappdemo.domain.repository.PriceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PriceRepositoryImpl @Inject constructor(private val dataSource: WebSocketDataSource) :
    PriceRepository {

    override fun observeData(identifiers: List<String>): Flow<PriceData> =
        dataSource.getTradingData(identifiers).retryWhen { cause, attempt ->
            delay(2000.milliseconds)
            true
        }.flowOn(Dispatchers.IO)

}