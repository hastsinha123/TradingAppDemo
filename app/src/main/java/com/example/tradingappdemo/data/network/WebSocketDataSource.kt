package com.example.tradingappdemo.data.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import javax.inject.Inject

class SocketClosedException(code: Int, reason: String) :
    IOException("WebSocket closed ($code): $reason")

class WebSocketDataSource @Inject constructor(
    private val client: OkHttpClient,
    private val parser: MarketMessageParser,
) : MarketDataSource {

    override fun connect(symbols: List<String>): Flow<SocketEvent> = callbackFlow {
        val request = Request.Builder().url(buildUrl(symbols)).build()

        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                trySend(SocketEvent.Opened)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parser.parse(text)?.let { trySend(SocketEvent.Received(it)) }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(NORMAL_CLOSURE, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close(SocketClosedException(code, reason))
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        })

        awaitClose { socket.cancel() }
    }

    private fun buildUrl(symbols: List<String>): String {
        val streams = symbols
            .flatMap { symbol ->
                val pair = symbol.lowercase()
                listOf("$pair@trade", "$pair@ticker")
            }
            .joinToString("/")
        return "$BASE_URL?streams=$streams"
    }

    private companion object {
        const val BASE_URL = "wss://stream.binance.com:9443/stream"
        const val NORMAL_CLOSURE = 1000
    }
}
