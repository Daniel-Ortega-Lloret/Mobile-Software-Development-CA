package com.example.mobiledevca_taskapp.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mobiledevca_taskapp.services.StepCounterService

class ScreenStateReceiver(private val service: StepCounterService) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                service.setAppActive(false)
                Log.d("screen_state", "screen is off")
            }
            else -> Log.d("screen_state", "Unknown action ${intent?.action}")
        }
    }
}