package com.example.tradingappdemo.domain.usecase

import com.example.tradingappdemo.domain.repository.PriceRepository
import javax.inject.Inject

class ObserveLatestPriceUseCase constructor(private val priceRepository: PriceRepository) {
}