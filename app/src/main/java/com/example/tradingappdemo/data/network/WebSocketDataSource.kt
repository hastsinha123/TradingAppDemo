package com.example.tradingappdemo.data.network

import com.example.tradingappdemo.domain.model.PriceData
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.IOException
import javax.inject.Inject

class WebSocketDataSource @Inject constructor(
    private val client: OkHttpClient, private val gson: Gson
) {

    fun getTradingData(identifiers: List<String>): Flow<PriceData> = callbackFlow {
        val stream = identifiers.joinToString("/") { "${it.lowercase()}@trade" }
        val request =
            Request.Builder().url("wss://stream.binance.com:9443/stream?streams=$stream").build()

        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val dta = runCatching { gson.fromJson(text, StreamData::class.java) }.getOrNull()
                    ?: return
                val price = dta.data.price.toDoubleOrNull() ?: return
                trySend(PriceData(dta.data.symbol, price))
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close(IOException("Socket Closed = $code due to : $reason"))
            }
        })

        awaitClose { socket.cancel() }
    }
}