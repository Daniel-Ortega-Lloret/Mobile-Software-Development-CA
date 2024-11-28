package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.mobiledevca_taskapp.R

object StepNotificationMaker {
    private val channelId = "StepGoalChannel"
    private val channelName = "Step Goal"

    fun createNotificationChannel(context:Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel (
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val nm: NotificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager
            nc.description = "Channel for step goal notifications"
            nc.vibrationPattern = longArrayOf(0, 1000)
            nc.enableVibration(true)
            nm.createNotificationChannel(nc)
        }
    }

    fun sendGoalNotification(context:Context, stepGoal: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_custom)
            .setContentTitle("Step Goal Reached")
            .setContentText("Congratulations on reaching your step goal!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        var nm: NotificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager
        nm.notify(1001, notification)
    }
}