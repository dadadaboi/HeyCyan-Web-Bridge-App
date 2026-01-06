package com.heycyan.glasses

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.sdk.glassessdksample.web.WebCommunicationService
import com.sdk.glassessdksample.web.GlassesCommandEvent
import com.sdk.glassessdksample.web.GlassesRequestEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@CapacitorPlugin(
    name = "HeyCyanGlasses",
    permissions = [
        Permission(strings = [Manifest.permission.BLUETOOTH_CONNECT], alias = "bluetoothConnect"),
        Permission(strings = [Manifest.permission.BLUETOOTH_SCAN], alias = "bluetoothScan"),
        Permission(strings = [Manifest.permission.ACCESS_FINE_LOCATION], alias = "location"),
        Permission(strings = [Manifest.permission.CAMERA], alias = "camera"),
        Permission(strings = [Manifest.permission.RECORD_AUDIO], alias = "microphone")
    ]
)
class HeyCyanGlassesPlugin : Plugin() {

    private var webCommunicationService: WebCommunicationService? = null
    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WebCommunicationService.WebCommunicationBinder
            webCommunicationService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            webCommunicationService = null
        }
    }

    override fun load() {
        super.load()
        // Bind to the existing WebCommunicationService from the HeyCyan app
        bindToService()
    }

    private fun bindToService() {
        val intent = Intent(activity, WebCommunicationService::class.java)
        try {
            activity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e("HeyCyanGlassesPlugin", "Failed to bind to WebCommunicationService", e)
        }
    }

    override fun start() {
        super.start()
        EventBus.getDefault().register(this)
    }

    override fun stop() {
        super.stop()
        EventBus.getDefault().unregister(this)
    }

    override fun destroy() {
        super.destroy()
        if (isBound) {
            try {
                activity.unbindService(connection)
                isBound = false
            } catch (e: IllegalArgumentException) {
                Log.w("HeyCyanGlassesPlugin", "Service not bound", e)
            }
        }
    }

    @PluginMethod
    fun startScan(call: PluginCall) {
        try {
            // Send command to the existing HeyCyan SDK via the WebCommunicationService
            if (isBound && webCommunicationService != null) {
                // In the actual implementation, this would trigger the scan in the HeyCyan SDK
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "startScan",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error starting scan", e)
        }
    }

    @PluginMethod
    fun stopScan(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "stopScan",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error stopping scan", e)
        }
    }

    @PluginMethod
    fun connect(call: PluginCall) {
        val deviceId = call.getString("deviceId") ?: ""
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "connect",
                    "deviceId" to deviceId,
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error connecting", e)
        }
    }

    @PluginMethod
    fun disconnect(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "disconnect",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error disconnecting", e)
        }
    }

    @PluginMethod
    fun getConnectionState(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("request", mapOf(
                    "request" to "getConnectionState",
                    "timestamp" to System.currentTimeMillis()
                ))
                // For now, return a mock response - in real implementation this would wait for the response
                call.resolve(mapOf("state" to "connected"))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error getting connection state", e)
        }
    }

    @PluginMethod
    fun getBattery(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("request", mapOf(
                    "request" to "getBattery",
                    "timestamp" to System.currentTimeMillis()
                ))
                // For now, return a mock response - in real implementation this would wait for the response
                call.resolve(mapOf("level" to 85, "isCharging" to false))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error getting battery", e)
        }
    }

    @PluginMethod
    fun getMediaInfo(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("request", mapOf(
                    "request" to "getMediaInfo",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve(mapOf(
                    "photos" to 12,
                    "videos" to 5,
                    "recordings" to 3
                ))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error getting media info", e)
        }
    }

    @PluginMethod
    fun getVolume(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("request", mapOf(
                    "request" to "getVolume",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve(mapOf("volume" to 75))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error getting volume", e)
        }
    }

    @PluginMethod
    fun getDeviceMode(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("request", mapOf(
                    "request" to "getDeviceMode",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve(mapOf("mode" to "idle"))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error getting device mode", e)
        }
    }

    @PluginMethod
    fun setDeviceMode(call: PluginCall) {
        val mode = call.getString("mode", "idle")
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "setDeviceMode",
                    "mode" to mode,
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error setting device mode", e)
        }
    }

    @PluginMethod
    fun setVolume(call: PluginCall) {
        val volume = call.getInt("volume", 50)
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "setVolume",
                    "volume" to volume,
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error setting volume", e)
        }
    }

    @PluginMethod
    fun takePhoto(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "takePhoto",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve(mapOf(
                    "path" to "/storage/photos/photo_${System.currentTimeMillis()}.jpg",
                    "timestamp" to System.currentTimeMillis()
                ))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error taking photo", e)
        }
    }

    @PluginMethod
    fun startVideo(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "startVideo",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error starting video", e)
        }
    }

    @PluginMethod
    fun stopVideo(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "stopVideo",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve(mapOf(
                    "path" to "/storage/videos/video_${System.currentTimeMillis()}.mp4",
                    "duration" to 10,
                    "timestamp" to System.currentTimeMillis()
                ))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error stopping video", e)
        }
    }

    @PluginMethod
    fun startAudioRecording(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "startAudioRecording",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error starting audio recording", e)
        }
    }

    @PluginMethod
    fun stopAudioRecording(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "stopAudioRecording",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve(mapOf(
                    "path" to "/storage/recordings/recording_${System.currentTimeMillis()}.mp3",
                    "duration" to 5,
                    "timestamp" to System.currentTimeMillis()
                ))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error stopping audio recording", e)
        }
    }

    @PluginMethod
    fun startAIRecognition(call: PluginCall) {
        val type = call.getString("type", "object")
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "startAIRecognition",
                    "type" to type,
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error starting AI recognition", e)
        }
    }

    @PluginMethod
    fun stopAIRecognition(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "stopAIRecognition",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error stopping AI recognition", e)
        }
    }

    @PluginMethod
    fun startTranslation(call: PluginCall) {
        val sourceLanguage = call.getString("sourceLanguage", "en")
        val targetLanguage = call.getString("targetLanguage", "zh")
        val text = call.getString("text", "")
        
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "startTranslation",
                    "sourceLanguage" to sourceLanguage,
                    "targetLanguage" to targetLanguage,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error starting translation", e)
        }
    }

    @PluginMethod
    fun stopTranslation(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "stopTranslation",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error stopping translation", e)
        }
    }

    @PluginMethod
    fun setVoiceWakeup(call: PluginCall) {
        val enabled = call.getBoolean("enabled", false)
        val keyword = call.getString("keyword", "HeyCyan")
        
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "setVoiceWakeup",
                    "enabled" to enabled,
                    "keyword" to keyword,
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error setting voice wakeup", e)
        }
    }

    @PluginMethod
    fun startWiFiTransfer(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "startWiFiTransfer",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve(mapOf(
                    "progress" to 0,
                    "totalFiles" to 10,
                    "transferredFiles" to 0
                ))
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error starting WiFi transfer", e)
        }
    }

    @PluginMethod
    fun stopWiFiTransfer(call: PluginCall) {
        try {
            if (isBound && webCommunicationService != null) {
                webCommunicationService?.sendToWebApp("command", mapOf(
                    "command" to "stopWiFiTransfer",
                    "timestamp" to System.currentTimeMillis()
                ))
                call.resolve()
            } else {
                call.reject("Service not bound")
            }
        } catch (e: Exception) {
            call.reject("Error stopping WiFi transfer", e)
        }
    }

    // Event handling methods to receive data from the HeyCyan SDK
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGlassesCommandEvent(event: GlassesCommandEvent) {
        // Handle commands coming from the web app through the WebCommunicationService
        Log.d("HeyCyanGlassesPlugin", "Received command: ${event.command}")
        // In a real implementation, this would notify Capacitor listeners
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGlassesRequestEvent(event: GlassesRequestEvent) {
        // Handle requests coming from the web app through the WebCommunicationService
        Log.d("HeyCyanGlassesPlugin", "Received request: ${event.request}")
        // In a real implementation, this would notify Capacitor listeners
    }
}