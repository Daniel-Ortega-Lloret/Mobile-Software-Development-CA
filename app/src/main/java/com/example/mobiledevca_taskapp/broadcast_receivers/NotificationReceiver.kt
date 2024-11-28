package com.example.mobiledevca_taskapp.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mobiledevca_taskapp.services.StepCounterService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action == "STOP_FOREGROUND_SERVICE") {
                Log.d("debug", "stopping service")
                val serviceIntent = Intent(context, StepCounterService::class.java)
                context?.stopService(serviceIntent)
            }
        }
    }
}