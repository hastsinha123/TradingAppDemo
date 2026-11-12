package com.example.tradingappdemo.domain.usecase

import com.example.tradingappdemo.domain.model.MarketSnapshot
import com.example.tradingappdemo.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class ObserveMarketUseCase @Inject constructor(
    private val repository: PriceRepository,
) {

    operator fun invoke(
        symbols: List<String>,
        seed: MarketSnapshot = MarketSnapshot(),
    ): Flow<MarketSnapshot> =
        repository.observeMarket(symbols)
            .scan(seed) { snapshot, event -> snapshot.applyEvent(event) }
}
