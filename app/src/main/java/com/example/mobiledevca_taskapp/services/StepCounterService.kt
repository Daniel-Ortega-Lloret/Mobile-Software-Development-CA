package com.example.mobiledevca_taskapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.common.ViewModelSharer
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitRepository

class StepCounterService : Service(), SensorEventListener {
    private lateinit var sensorManager : SensorManager
    private var stepCounter: Sensor? = null
    private var previousTotalSteps: Float = 0f
    private var taskViewModel: TaskViewModel? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("debug", "I be da step sensorrrr")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepCounter?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: Toast.makeText(this, "Your device does not have a pedometer for this activity", Toast.LENGTH_LONG).show()
    }

    //Keeps service running in the background
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        taskViewModel = ViewModelSharer.taskViewModel
        val notification = createNotification()
        startForeground(1, notification)

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("debug", "i be shtepping rn")
        if (event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (previousTotalSteps == 0f) {
                previousTotalSteps = event.values[0]
            }

            val currentSteps = event.values[0] - previousTotalSteps
            Log.d("debug", "$currentSteps")
            taskViewModel?.updateStepCount(currentSteps)

            previousTotalSteps = event.values[0]

        }
    }

    private fun createNotification(): Notification {
        val channelId = "StepCounterChannel"
        val channelName = "Step Counter"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Step Counter Active")
            .setContentText("Tracking your steps in the background")
            .setSmallIcon(R.mipmap.ic_launcher_custom)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    //Handles changes to accuracy, but we don't do that so it stays empty since it needs to be implemented
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    //Not a bound service so I return null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}