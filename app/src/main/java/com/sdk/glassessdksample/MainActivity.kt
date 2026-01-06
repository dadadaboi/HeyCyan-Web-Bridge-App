package com.sdk.glassessdksample

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyListener
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyRsp
import com.sdk.glassessdksample.databinding.AcitivytMainBinding
import com.sdk.glassessdksample.ui.BluetoothUtils
import com.sdk.glassessdksample.ui.DeviceBindActivity
import com.sdk.glassessdksample.ui.hasBluetooth
import com.sdk.glassessdksample.ui.requestAllPermission
import com.sdk.glassessdksample.ui.requestBluetoothPermission
import com.sdk.glassessdksample.ui.requestLocationPermission
import com.sdk.glassessdksample.ui.setOnClickListener
import com.sdk.glassessdksample.ui.startKtxActivity
import com.sdk.glassessdksample.web.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private lateinit var binding: AcitivytMainBinding
    private val deviceNotifyListener by lazy { MyDeviceNotifyListener() }
    
    // Web communication
    private var webCommunicationService: WebCommunicationService? = null
    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WebCommunicationService.WebCommunicationBinder
            webCommunicationService = binder.getService()
            isBound = true
            
            // Connect to your web application
            // Replace with your actual web app URL
            webCommunicationService?.connectToWebApp("https://your-web-app.com")
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            webCommunicationService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivytMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Bind to web communication service
        bindService()
        
        // Register for EventBus
        EventBus.getDefault().register(this)
        
        initView()
    }
    
    private fun bindService() {
        val intent = Intent(this, WebCommunicationService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Unbind from web communication service
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        
        // Unregister from EventBus
        EventBus.getDefault().unregister(this)
    }
    inner class PermissionCallback : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
            if (!all) {

            }else{
                startKtxActivity<DeviceBindActivity>()
            }
        }

        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
            super.onDenied(permissions, never)
            if(never){
                XXPermissions.startPermissionActivity(this@MainActivity, permissions);
            }
        }

    }


    override fun onResume() {
        super.onResume()
        try {
            if (!BluetoothUtils.isEnabledBluetooth(this)) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                }
                startActivityForResult(intent, 300)
            }
        } catch (e: Exception) {
        }
        if (!hasBluetooth(this)) {
            requestBluetoothPermission(this, BluetoothPermissionCallback())
        }

        requestAllPermission(this, OnPermissionCallback { permissions, all ->  })
    }

    inner class BluetoothPermissionCallback : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
            if (!all) {

            }
        }

        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
            super.onDenied(permissions, never)
            if (never) {
                XXPermissions.startPermissionActivity(this@MainActivity, permissions)
            }
        }

    }

    private fun initView() {
        setOnClickListener(
            binding.btnScan,
            binding.btnConnect,
            binding.btnDisconnect,
            binding.btnAddListener,
            binding.btnSetTime,
            binding.btnVersion,
            binding.btnCamera,
            binding.btnVideo,
            binding.btnRecord,
            binding.btnThumbnail,
            binding.btnBt,
            binding.btnBattery,
            binding.btnVolume,
            binding.btnMediaCount
        ) {
            when (this) {
                binding.btnScan -> {
                    requestLocationPermission(this@MainActivity, PermissionCallback())
                }

                binding.btnConnect -> {
                    BleOperateManager.getInstance()
                        .connectDirectly(DeviceManager.getInstance().deviceAddress)
                    
                    // Send connection status to web app
                    sendDeviceStatus("connection", "connected")
                }

                binding.btnDisconnect -> {
                    BleOperateManager.getInstance().unBindDevice()
                    
                    // Send disconnection status to web app
                    sendDeviceStatus("connection", "disconnected")
                }

                binding.btnAddListener -> {
                    LargeDataHandler.getInstance().addOutDeviceListener(100, deviceNotifyListener)
                }

                binding.btnSetTime -> {
                    Log.i("setTime", "setTime"+BleOperateManager.getInstance().isConnected)
                    LargeDataHandler.getInstance().syncTime { _, _ -> }
                }

                binding.btnVersion -> {
                    LargeDataHandler.getInstance().syncDeviceInfo { _, response ->
                        if (response != null) {
                            //wifi 固件版本
                             response.wifiFirmwareVersion
                            //wifi 产品版本
                            response.wifiHardwareVersion
                            //蓝牙产品版本
                             response.hardwareVersion
                            //蓝牙固件版本
                             response.firmwareVersion
                        }
                    }
                }

                binding.btnCamera -> {
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(0x02, 0x01, 0x01)
                    ) { _, it ->
                        if (it.dataType == 1 && it.errorCode == 0) {
                            when (it.workTypeIng) {
                                2 -> {
                                    //眼镜正在录像
                                    sendDeviceStatus("camera", "recording_video")
                                }
                                4 -> {
                                    //眼镜正在传输模式
                                    sendDeviceStatus("camera", "transfer_mode")
                                }
                                5 -> {
                                    //眼镜正在OTA模式
                                    sendDeviceStatus("camera", "ota_mode")
                                }
                                1, 6 ->{
                                    //眼镜正在拍照模式
                                    sendDeviceStatus("camera", "taking_photo")
                                }
                                7 -> {
                                    //眼镜正在AI对话
                                    sendDeviceStatus("camera", "ai_conversation")
                                }
                                8 ->{
                                    //眼镜正在录音模式
                                    sendDeviceStatus("camera", "recording_audio")
                                }
                            }
                        } else {
                            //执行开始和结束
                            sendDeviceStatus("camera", "idle")
                        }
                    }
                }

                binding.btnVideo -> {
                    //videoStart  true 开始录制   false 停止录制
                    val videoStart=true
                    val value = if (videoStart) 0x02 else 0x03
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(0x02, 0x01, value.toByte())
                    ) { _, it ->
                        if (it.dataType == 1) {
                            if (it.errorCode == 0) {
                                when (it.workTypeIng) {
                                    2 -> {
                                        //眼镜正在录像
                                        sendDeviceStatus("video", "recording")
                                    }
                                    4 -> {
                                        //眼镜正在传输模式
                                        sendDeviceStatus("video", "transfer_mode")
                                    }
                                    5 -> {
                                        //眼镜正在OTA模式
                                        sendDeviceStatus("video", "ota_mode")
                                    }
                                    1, 6 ->{
                                        //眼镜正在拍照模式
                                        sendDeviceStatus("video", "taking_photo")
                                    }
                                    7 -> {
                                        //眼镜正在AI对话
                                        sendDeviceStatus("video", "ai_conversation")
                                    }
                                    8 ->{
                                        //眼镜正在录音模式
                                        sendDeviceStatus("video", "recording_audio")
                                    }
                                }
                            } else {
                                //执行开始和结束
                                sendDeviceStatus("video", "idle")
                            }
                        }
                    }
                }

                binding.btnRecord -> {
                    //recordStart  true 开始录制   false 停止录制
                    val recordStart=true
                    val value = if (recordStart) 0x08 else 0x0c
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(0x02, 0x01, value.toByte())
                    ) { _, it ->
                        if (it.dataType == 1) {
                            if (it.errorCode == 0) {
                                when (it.workTypeIng) {
                                    2 -> {
                                        //眼镜正在录像
                                    }
                                    4 -> {
                                        //眼镜正在传输模式
                                    }
                                    5 -> {
                                        //眼镜正在OTA模式
                                    }
                                    1, 6 ->{
                                        //眼镜正在拍照模式
                                    }
                                    7 -> {
                                        //眼镜正在AI对话
                                    }
                                    8 ->{
                                        //眼镜正在录音模式
                                    }
                                }
                            } else {
                                //执行开始和结束
                            }
                        }
                    }
                }

                binding.btnThumbnail -> {
                    //thumbnailSize  0..6
                    val thumbnailSize=0x02
                    LargeDataHandler.getInstance().glassesControl(
                        byteArrayOf(
                            0x02,
                            0x01,
                            0x06,
                            thumbnailSize.toByte(),
                            thumbnailSize.toByte(),
                            0x02
                        )
                    ) { _, it ->
                        if (it.dataType == 1) {
                            if (it.errorCode == 0) {
                                when (it.workTypeIng) {
                                    2 -> {
                                        //眼镜正在录像
                                    }
                                    4 -> {
                                        //眼镜正在传输模式
                                    }
                                    5 -> {
                                        //眼镜正在OTA模式
                                    }
                                    1, 6 ->{
                                        //眼镜正在拍照模式
                                    }
                                    7 -> {
                                        //眼镜正在AI对话
                                    }
                                    8 ->{
                                        //眼镜正在录音模式
                                    }
                                }
                            } else {
                                //触发AI拍照，上报缩略图会收到上报指令
                            }
                        }
                    }
                }

                binding.btnBt -> {
                    //BT扫描
                    BleOperateManager.getInstance().classicBluetoothStartScan()

                }
                binding.btnBattery -> {
                    //添加电量监听
                    LargeDataHandler.getInstance().addBatteryCallBack("init") { _, response ->
                        // Send battery status to web app when received
                        if (isBound && webCommunicationService != null) {
                            webCommunicationService?.sendToDeviceData("battery_request", "callback_received")
                        }
                    }
                    //电量
                    LargeDataHandler.getInstance().syncBattery()
                    
                    // Send battery request status to web app
                    sendDeviceStatus("battery_request", "sent")
                }
                binding.btnVolume ->{
                    //读取音量控制
                    LargeDataHandler.getInstance().getVolumeControl { _, response ->
                        if (response != null) {
                            //眼镜音量 音乐最小值 最大值 当前值
                            response.minVolumeMusic
                            response.maxVolumeMusic
                            response.currVolumeMusic
                            //眼镜电话 电话最小值 最大值 当前值
                            response.minVolumeCall
                            response.maxVolumeCall
                            response.currVolumeCall
                            //眼镜系统 系统最小值 最大值 当前值
                            response.minVolumeSystem
                            response.maxVolumeSystem
                            response.currVolumeSystem
                            //眼镜当前的模式
                            response.currVolumeType
                        }
                    }
                }
                binding.btnMediaCount ->{
                    LargeDataHandler.getInstance().glassesControl(byteArrayOf(0x02, 0x04)) { _, it ->
                        if (it.dataType == 4) {
                            val mediaCount = it.imageCount + it.videoCount + it.recordCount
                            if (mediaCount > 0) {
                                //眼镜有多少个媒体没有上传
                            } else {
                                //无
                            }
                        }
                    }
                }
            }
        }
    }

    inner class MyDeviceNotifyListener : GlassesDeviceNotifyListener() {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun parseData(cmdType: Int, response: GlassesDeviceNotifyRsp) {
            when (response.loadData[6].toInt()) {
                //眼镜电量上报
                0x05 -> {
                    //当前电量
                    val battery = response.loadData[7].toInt()
                    //是否在充电
                    val changing = response.loadData[8].toInt()
                    
                    // Send battery info to web app
                    if (isBound && webCommunicationService != null) {
                        val batteryInfo = mapOf(
                            "level" to battery,
                            "isCharging" to (changing == 1)
                        )
                        webCommunicationService?.sendToDeviceData("battery", batteryInfo)
                    }
                }
                //眼镜通过快捷识别
                0x02 -> {
                    if (response.loadData.size > 9 && response.loadData[9].toInt() == 0x02) {
                        //要设置识别意图：eg 请帮我看看眼前是什么，图片中的内容
                    }
                    //获取图片缩略图
                    LargeDataHandler.getInstance().getPictureThumbnails { cmdType, success, data ->
                        //请将data存入路径,jpg的图片
                        // Send thumbnail info to web app
                        if (isBound && webCommunicationService != null) {
                            val thumbnailInfo = mapOf(
                                "success" to success,
                                "dataSize" to data?.size
                            )
                            webCommunicationService?.sendToDeviceData("thumbnail", thumbnailInfo)
                        }
                    }
                }

                0x03 -> {
                    if (response.loadData[7].toInt() == 1) {
                        //眼镜启动麦克风开始说话
                        if (isBound && webCommunicationService != null) {
                            webCommunicationService?.sendToDeviceStatus("microphone", "active")
                        }
                    }
                }
                //ota 升级
                0x04 -> {
                    try {
                        val download = response.loadData[7].toInt()
                        val soc = response.loadData[8].toInt()
                        val nor = response.loadData[9].toInt()
                        //download 固件下载进度 soc 下载进度 nor 升级进度
                        
                        // Send OTA status to web app
                        if (isBound && webCommunicationService != null) {
                            val otaStatus = mapOf(
                                "downloadProgress" to download,
                                "socProgress" to soc,
                                "norProgress" to nor
                            )
                            webCommunicationService?.sendToDeviceData("ota", otaStatus)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                0x0c -> {
                    //眼镜触发暂停事件，语音播报
                    if (response.loadData[7].toInt() == 1) {
                        //to do
                        if (isBound && webCommunicationService != null) {
                            webCommunicationService?.sendToDeviceStatus("speech", "paused")
                        }
                    }
                }

                0x0d -> {
                    //解除APP绑定事件
                    if (response.loadData[7].toInt() == 1) {
                        //to do
                        if (isBound && webCommunicationService != null) {
                            webCommunicationService?.sendToDeviceStatus("binding", "unbound")
                        }
                    }
                }
                //眼镜内存不足事件
                0x0e -> {
                    if (isBound && webCommunicationService != null) {
                        webCommunicationService?.sendToDeviceStatus("storage", "insufficient")
                    }
                }
                //翻译暂停事件
                0x10 -> {
                    if (isBound && webCommunicationService != null) {
                        webCommunicationService?.sendToDeviceStatus("translation", "paused")
                    }
                }
                //眼镜音量变化事件
                0x12 -> {
                    //音乐音量
                    //最小音量
                    val musicMin = response.loadData[8].toInt()
                    //最大音量
                    val musicMax = response.loadData[9].toInt()
                    //当前音量
                    val musicCurrent = response.loadData[10].toInt()

                    //来电音量
                    //最小音量
                    val callMin = response.loadData[12].toInt()
                    //最大音量
                    val callMax = response.loadData[13].toInt()
                    //当前音量
                    val callCurrent = response.loadData[14].toInt()

                    //眼镜系统音量
                    //最小音量
                    val systemMin = response.loadData[16].toInt()
                    //最大音量
                    val systemMax = response.loadData[17].toInt()
                    //当前音量
                    val systemCurrent = response.loadData[18].toInt()

                    //当前的音量模式
                    val volumeMode = response.loadData[19].toInt()
                    
                    // Send volume info to web app
                    if (isBound && webCommunicationService != null) {
                        val volumeInfo = mapOf(
                            "music" to mapOf(
                                "min" to musicMin,
                                "max" to musicMax,
                                "current" to musicCurrent
                            ),
                            "call" to mapOf(
                                "min" to callMin,
                                "max" to callMax,
                                "current" to callCurrent
                            ),
                            "system" to mapOf(
                                "min" to systemMin,
                                "max" to systemMax,
                                "current" to systemCurrent
                            ),
                            "mode" to volumeMode
                        )
                        webCommunicationService?.sendToDeviceData("volume", volumeInfo)
                    }
                }
            }
        }
    }
    
    // Event handling methods for web communication
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGlassesCommandEvent(event: GlassesCommandEvent) {
        when (event.command) {
            "takePhoto" -> {
                // Trigger the same action as btnCamera
                LargeDataHandler.getInstance().glassesControl(
                    byteArrayOf(0x02, 0x01, 0x01)
                ) { _, it ->
                    if (it.dataType == 1 && it.errorCode == 0) {
                        webCommunicationService?.sendToDeviceStatus("photo_taken", true)
                    } else {
                        webCommunicationService?.sendToDeviceStatus("photo_taken", false)
                    }
                }
            }
            "startRecording" -> {
                LargeDataHandler.getInstance().glassesControl(
                    byteArrayOf(0x02, 0x01, 0x02)
                ) { _, it ->
                    if (it.dataType == 1) {
                        if (it.errorCode == 0) {
                            webCommunicationService?.sendToDeviceStatus("recording_started", true)
                        } else {
                            webCommunicationService?.sendToDeviceStatus("recording_started", false)
                        }
                    }
                }
            }
            "stopRecording" -> {
                LargeDataHandler.getInstance().glassesControl(
                    byteArrayOf(0x02, 0x01, 0x03)
                ) { _, it ->
                    if (it.dataType == 1) {
                        if (it.errorCode == 0) {
                            webCommunicationService?.sendToDeviceStatus("recording_stopped", true)
                        } else {
                            webCommunicationService?.sendToDeviceStatus("recording_stopped", false)
                        }
                    }
                }
            }
            "getBattery" -> {
                LargeDataHandler.getInstance().syncBattery()
            }
            else -> {
                Log.d("MainActivity", "Unknown command: ${'$'}{event.command}")
            }
        }
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGlassesRequestEvent(event: GlassesRequestEvent) {
        when (event.request) {
            "getDeviceInfo" -> {
                LargeDataHandler.getInstance().syncDeviceInfo { _, response ->
                    if (response != null) {
                        val deviceInfo = mapOf(
                            "firmwareVersion" to response.firmwareVersion,
                            "hardwareVersion" to response.hardwareVersion,
                            "wifiFirmwareVersion" to response.wifiFirmwareVersion,
                            "wifiHardwareVersion" to response.wifiHardwareVersion
                        )
                        webCommunicationService?.sendToDeviceData("deviceInfo", deviceInfo)
                    }
                }
            }
            "getMediaCount" -> {
                LargeDataHandler.getInstance().glassesControl(byteArrayOf(0x02, 0x04)) { _, it ->
                    if (it.dataType == 4) {
                        val mediaCount = mapOf(
                            "imageCount" to it.imageCount,
                            "videoCount" to it.videoCount,
                            "recordCount" to it.recordCount
                        )
                        webCommunicationService?.sendToDeviceData("mediaCount", mediaCount)
                    }
                }
            }
        }
    }
    
    // Method to send device status to web app
    private fun sendDeviceStatus(status: String, value: Any) {
        webCommunicationService?.sendToDeviceStatus(status, value)
    }
    
    // Method to send device data to web app
    private fun sendDeviceData(dataType: String, data: Any) {
        webCommunicationService?.sendToDeviceData(dataType, data)
    }
}