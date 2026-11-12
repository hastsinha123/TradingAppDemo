package com.example.tradingappdemo.data.network

import com.example.tradingappdemo.domain.model.MarketEvent
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import javax.inject.Inject

class MarketMessageParser @Inject constructor() {

    fun parse(text: String): MarketEvent? {
        val root = try {
            JsonParser.parseString(text)
        } catch (e: JsonParseException) {
            return null
        }
        val data = root.asObjectOrNull()?.get("data")?.asObjectOrNull() ?: return null
        val symbol = data.string("s") ?: return null

        return when (data.string("e")) {
            EVENT_TRADE -> {
                val price = data.positiveDouble("p") ?: return null
                MarketEvent.Trade(symbol, price)
            }

            EVENT_TICKER -> {
                val lastPrice = data.positiveDouble("c") ?: return null
                val openPrice = data.positiveDouble("o") ?: return null
                MarketEvent.Stats24h(symbol, lastPrice, openPrice)
            }

            else -> null
        }
    }

    private fun JsonElement.asObjectOrNull(): JsonObject? = if (isJsonObject) asJsonObject else null

    private fun JsonObject.string(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString

    private fun JsonObject.positiveDouble(key: String): Double? =
        string(key)?.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }

    private companion object {
        const val EVENT_TRADE = "trade"
        const val EVENT_TICKER = "24hrTicker"
    }
}
