package com.example.mobiledevca_taskapp.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mobiledevca_taskapp.services.StepCounterService

//Re-starts the step counter after a device reboot
class HabitStepCountReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                val serviceIntent = Intent(context, StepCounterService::class.java)
                context?.startService(serviceIntent)
            }
        }
    }
}