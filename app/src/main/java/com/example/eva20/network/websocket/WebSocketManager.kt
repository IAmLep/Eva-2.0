package com.example.eva20.network.websocket

import com.example.eva20.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager {
    private val tag = "WebSocketManager"
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private var connectionStatusListener: ((String) -> Unit)? = null
    private var messageListener: ((String) -> Unit)? = null

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun connect() {
        val request = Request.Builder()
            .url("YOUR_WEBSOCKET_URL") // Will be updated later when WebSockets are enabled
            .build()

        webSocket = client.newWebSocket(request, createWebSocketListener())
        Logger.d(tag, "Connecting to WebSocket...")
    }

    fun disconnect() {
        webSocket?.close(1000, "User closed connection")
        webSocket = null
        Logger.d(tag, "Disconnected from WebSocket")
    }

    fun sendCommand(command: String) {
        scope.launch {
            try {
                val jsonObject = JSONObject().apply {
                    put("type", "command")
                    put("command", command)
                    put("timestamp", System.currentTimeMillis())
                }

                webSocket?.send(jsonObject.toString())
                Logger.d(tag, "Sent command: $command")
            } catch (e: Exception) {
                Logger.e(tag, "Error sending command", e)
            }
        }
    }

    fun setOnConnectionStatusChangeListener(listener: (String) -> Unit) {
        connectionStatusListener = listener
    }

    @Suppress("unused")
    fun setOnMessageListener(listener: (String) -> Unit) {
        messageListener = listener
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                connectionStatusListener?.invoke("Connected")
                Logger.d(tag, "WebSocket connection established")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                messageListener?.invoke(text)
                Logger.d(tag, "Received message: $text")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                connectionStatusListener?.invoke("Closing: $reason")
                Logger.d(tag, "WebSocket closing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                connectionStatusListener?.invoke("Closed: $reason")
                Logger.d(tag, "WebSocket closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                connectionStatusListener?.invoke("Failed: ${t.message}")
                Logger.e(tag, "WebSocket failure", t)
            }
        }
    }
}