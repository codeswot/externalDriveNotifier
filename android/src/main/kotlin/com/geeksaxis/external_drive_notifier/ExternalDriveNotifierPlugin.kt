package com.geeksaxis.external_drive_notifier

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class ExternalDriveNotifierPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel : MethodChannel
    private lateinit var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null
    private lateinit var context: Context
    private var activity: Activity? = null
    private var receiver: BroadcastReceiver? = null
    private var lastKnownDrives: List<String> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private val driveCheckRunnable = object : Runnable {
        override fun run() {
            checkForDriveChanges()
            handler.postDelayed(this, DRIVE_CHECK_INTERVAL)
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "external_drive_notifier")
        channel.setMethodCallHandler(this)

        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "external_drive_notifier_events")
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                eventSink = events
                registerReceiver()
                startDriveChecking()
                // Initial check
                handler.post {
                    checkForDriveChanges()
                }
            }

            override fun onCancel(arguments: Any?) {
                eventSink = null
                unregisterReceiver()
                stopDriveChecking()
            }
        })

        context = flutterPluginBinding.applicationContext
    }

    private fun startDriveChecking() {
        handler.postDelayed(driveCheckRunnable, DRIVE_CHECK_INTERVAL)
    }

    private fun stopDriveChecking() {
        handler.removeCallbacks(driveCheckRunnable)
    }

    private fun checkForDriveChanges() {
        val currentDrives = getExternalDrives()
        if (currentDrives != lastKnownDrives) {
            val added = currentDrives.filter { it !in lastKnownDrives }
            val removed = lastKnownDrives.filter { it !in currentDrives }

            for (drive in added) {
                notifyEvent("mounted", drive)
            }

            for (drive in removed) {
                notifyEvent("removed", drive)
            }

            lastKnownDrives = currentDrives
        }
    }

    private fun registerReceiver() {
        if (receiver != null) {
            return
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    UsbReceiver.ACTION_USB_ATTACHED -> {
                        handler.postDelayed({ checkForDriveChanges() }, 1000) // Delay to allow mount
                        notifyEvent("attached", null)
                    }
                    UsbReceiver.ACTION_USB_DETACHED -> {
                        checkForDriveChanges()
                        notifyEvent("detached", null)
                    }
                    Intent.ACTION_MEDIA_MOUNTED -> {
                        checkForDriveChanges()
                        notifyEvent("mounted", intent.data?.path)
                    }
                    Intent.ACTION_MEDIA_UNMOUNTED -> {
                        checkForDriveChanges()
                        notifyEvent("unmounted", intent.data?.path)
                    }
                    Intent.ACTION_MEDIA_REMOVED -> {
                        checkForDriveChanges()
                        notifyEvent("removed", intent.data?.path)
                    }
                }
            }
        }

        // Register for USB events
        val usbFilter = IntentFilter().apply {
            addAction(UsbReceiver.ACTION_USB_ATTACHED)
            addAction(UsbReceiver.ACTION_USB_DETACHED)
        }
        context.registerReceiver(receiver, usbFilter)

        // Register for media events
        val mediaFilter = IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_REMOVED)
            addDataScheme("file")
        }
        context.registerReceiver(receiver, mediaFilter)

        // Store initial state
        lastKnownDrives = getExternalDrives()
    }

    private fun notifyEvent(event: String, path: String?) {
        eventSink?.success(mapOf(
            "event" to event,
            "path" to path
        ))

        // Then send the updated drives list
        val drives = getExternalDrives()
        eventSink?.success(mapOf(
            "event" to "drives_updated",
            "drives" to drives
        ))
    }

    private fun unregisterReceiver() {
        receiver?.let {
            try {
                context.unregisterReceiver(it)
                receiver = null
            } catch (e: Exception) {
                // Receiver not registered
            }
        }
        stopDriveChecking()
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getExternalDrives" -> {
                val drives = getExternalDrives()
                result.success(drives)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getExternalDrives(): List<String> {
        val drives = mutableListOf<String>()
        
        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                for (storageVolume in storageManager.storageVolumes) {
                    if (storageVolume.isRemovable) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            storageVolume.directory?.absolutePath?.let { path ->
                                drives.add(path)
                            }
                        } else {
                            try {
                                val getPathMethod = storageVolume.javaClass.getMethod("getPath")
                                val path = getPathMethod.invoke(storageVolume) as String
                                drives.add(path)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } else {
                val externalDirs = context.getExternalFilesDirs(null)
                for (dir in externalDirs) {
                    if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                        val path = dir.path.split("/Android")[0]
                        if (!drives.contains(path)) {
                            drives.add(path)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return drives.distinct()
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        unregisterReceiver()
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    companion object {
        object UsbReceiver {
            const val ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
            const val ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
        }
        private const val DRIVE_CHECK_INTERVAL: Long = 2000 // 2 seconds
    }
}