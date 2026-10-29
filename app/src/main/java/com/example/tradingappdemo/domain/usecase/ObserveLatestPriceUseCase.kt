package com.example.tradingappdemo.domain.usecase

import com.example.tradingappdemo.domain.model.Stock
import com.example.tradingappdemo.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class ObserveLatestPriceUseCase @Inject constructor(private val priceRepository: PriceRepository) {

    fun getStocksFlow(stocks : List<Stock>) : Flow<Map<String, Double>> {
        return priceRepository.observeData(stocks.map { it.identifier }).scan(emptyMap()){ current, data ->
            current + (data.symbol to data.price)
        }
    }

}