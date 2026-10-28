package com.example.tradingappdemo.data.network

import com.google.gson.annotations.SerializedName


data class StreamData(val data: TradePrice)

data class TradePrice(
    @SerializedName("s") val symbol: String,
    @SerializedName("p") val price: String,
)