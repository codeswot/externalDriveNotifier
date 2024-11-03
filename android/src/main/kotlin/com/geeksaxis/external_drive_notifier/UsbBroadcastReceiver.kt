package com.geeksaxis.external_drive_notifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UsbBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String? = intent?.action
        if (action != null) {
            when (action) {
                Intent.ACTION_MEDIA_MOUNTED -> {
                    Log.d("UsbBroadcastReceiver", "USB Drive Inserted")
                    // Handle USB drive insertion here, e.g., notify Flutter
                }
                Intent.ACTION_MEDIA_REMOVED, Intent.ACTION_MEDIA_EJECT -> {
                    Log.d("UsbBroadcastReceiver", "USB Drive Removed")
                    // Handle USB drive removal here, e.g., notify Flutter
                }
            }
        }
    }
}
