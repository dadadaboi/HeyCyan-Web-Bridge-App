package com.sdk.glassessdksample.web

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebCommunicationService : Service() {

    private val binder = WebCommunicationBinder()
    private var httpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var webAppUrl: String? = null
    private val gson = Gson()

    inner class WebCommunicationBinder : Binder() {
        fun getService(): WebCommunicationService = this@WebCommunicationService
    }

    override fun onCreate() {
        super.onCreate()
        initializeHttpClient()
    }

    private fun initializeHttpClient() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun connectToWebApp(url: String) {
        this.webAppUrl = url
        connectWebSocket()
    }

    private fun connectWebSocket() {
        if (webAppUrl == null) return

        val wsUrl = webAppUrl!!.replace("http", "ws") + "/ws/glasses"
        val request = Request.Builder().url(wsUrl).build()

        webSocket = httpClient?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebCommunication", "WebSocket connected to web app")
                // Send device registration message
                sendToDeviceStatus("connected", true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebCommunication", "Received message from web: $text")
                handleWebAppMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebCommunication", "WebSocket error: ${t.message}")
                // Attempt to reconnect after delay
                Thread {
                    Thread.sleep(5000)
                    connectWebSocket()
                }.start()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebCommunication", "WebSocket closed: $reason")
            }
        })
    }

    private fun handleWebAppMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.getString("type")
            
            when (type) {
                "command" -> {
                    val command = json.getString("command")
                    val params = if (json.has("params")) json.getJSONObject("params") else null
                    executeRemoteCommand(command, params)
                }
                "request" -> {
                    val request = json.getString("request")
                    val params = if (json.has("params")) json.getJSONObject("params") else null
                    handleRemoteRequest(request, params)
                }
            }
        } catch (e: Exception) {
            Log.e("WebCommunication", "Error parsing web message: ${e.message}")
        }
    }

    private fun executeRemoteCommand(command: String, params: JSONObject?) {
        Log.d("WebCommunication", "Executing remote command: $command")
        
        // Send the command to the main app via EventBus
        val event = GlassesCommandEvent(command, params)
        EventBus.getDefault().post(event)
    }

    private fun handleRemoteRequest(request: String, params: JSONObject?) {
        Log.d("WebCommunication", "Handling remote request: $request")
        
        when (request) {
            "getDeviceInfo" -> {
                // This would be handled by the main activity
                val event = GlassesRequestEvent("getDeviceInfo", params)
                EventBus.getDefault().post(event)
            }
            "getMediaCount" -> {
                val event = GlassesRequestEvent("getMediaCount", params)
                EventBus.getDefault().post(event)
            }
        }
    }

    fun sendToWebApp(type: String, data: Any) {
        if (webSocket == null) {
            Log.w("WebCommunication", "WebSocket not connected, queuing message")
            // Could implement message queue here
            return
        }

        try {
            val payload = mutableMapOf<String, Any>()
            payload["type"] = type
            payload["data"] = data
            payload["timestamp"] = System.currentTimeMillis()
            
            val json = gson.toJson(payload)
            webSocket?.send(json)
            Log.d("WebCommunication", "Sent to web: $json")
        } catch (e: Exception) {
            Log.e("WebCommunication", "Error sending to web: ${e.message}")
        }
    }

    fun sendToDeviceStatus(status: String, value: Any) {
        val statusData = mapOf(
            "status" to status,
            "value" to value,
            "timestamp" to System.currentTimeMillis()
        )
        sendToWebApp("status", statusData)
    }

    fun sendToDeviceData(dataType: String, data: Any) {
        val deviceData = mapOf(
            "dataType" to dataType,
            "data" to data,
            "timestamp" to System.currentTimeMillis()
        )
        sendToWebApp("data", deviceData)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "Service destroyed")
        httpClient?.dispatcher?.executorService?.shutdown()
        httpClient?.connectionPool?.evictAll()
    }
}