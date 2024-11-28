package com.example.mobiledevca_taskapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.broadcast_receivers.ScreenStateReceiver
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel

class StepCounterService : Service(), SensorEventListener {
    private lateinit var sensorManager : SensorManager
    private var stepCounter: Sensor? = null
    private var previousTotalSteps: Float = 0f
    private var taskViewModel: TaskViewModel? = null
    private val binder = StepCounterBinder()
    private var isAppActive: Boolean = false
    private var screenStateReceiver : ScreenStateReceiver? = null
    private var accumulatedSteps: Float = 0f

    override fun onCreate() {
        super.onCreate()

        val notification = createNotification()
        startForeground(1, notification)

        if (checkPermission()) {
            startStepTracking()
            registerReceiver()
            schedulePeriodicSave()
        } else {
            Log.e("StepCounterService", "Permissions not granted")
            stopSelf()
        }

    }

    private fun checkPermission(): Boolean {
        val activityRecognitionPermission = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        )

        val foregroundServicePermission = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.FOREGROUND_SERVICE
        )


        val bodySensorsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BODY_SENSORS
            )
        } else {
            PackageManager.PERMISSION_GRANTED // No need to check for BODY_SENSORS for older versions
        }

        // All permissions must be granted
        return activityRecognitionPermission == PackageManager.PERMISSION_GRANTED &&
                foregroundServicePermission == PackageManager.PERMISSION_GRANTED &&
                bodySensorsPermission == PackageManager.PERMISSION_GRANTED
    }


    private fun registerReceiver() {
        screenStateReceiver = ScreenStateReceiver(this)
        registerReceiver(screenStateReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    private fun startStepTracking() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepCounter?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: Toast.makeText(this, "Your device does not have a step counter for this activity", Toast.LENGTH_LONG).show()
    }

    private fun schedulePeriodicSave() {
        val saveIntervalMilliseconds = 15 * 60 * 1000L //15 mins in milliseconds

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAppActive) {
                taskViewModel?.updateStepCount(previousTotalSteps.toInt())
            }
            schedulePeriodicSave() //Re-schedule the timer
        }, saveIntervalMilliseconds)
    }

    //Keeps service running in the background
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val currentStepCount = event.values[0].toInt()

        val deltaSteps = currentStepCount - previousTotalSteps

        previousTotalSteps = currentStepCount.toFloat()

        // Only add one step if delta is positive
        if (deltaSteps > 0) {
            if (isAppActive) {
                taskViewModel?.updateStepCount(1)
                Log.d("debug", "App active: added 1 step")
            } else {
                accumulatedSteps += 1f
                Log.d("debug", "App inactive: accumulated steps = $accumulatedSteps")
            }
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) //Make it persist so that user knows service is running in the background
            .build()
    }

    //Handles changes to accuracy, but we don't do that so it stays empty since it needs to be implemented
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onDestroy() {
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
        screenStateReceiver?.let {
            unregisterReceiver(it)
        }
        super.onDestroy()
    }


    //Not a bound service so I return null
    override fun onBind(intent: Intent?): IBinder = binder

    inner class StepCounterBinder: Binder() {
        fun setTaskViewModel(viewModel: TaskViewModel) {
            this@StepCounterService.setTaskViewModel(viewModel)
        }
        fun getService(): StepCounterService = this@StepCounterService
    }

    fun setTaskViewModel(viewModel: TaskViewModel) {
        taskViewModel = viewModel
    }

    fun setAppActive(isActive: Boolean) {
        this.isAppActive = isActive
        if (isActive) {
            Log.d("appState", "true")
            pushPendingSteps()
        }
        else {
            Log.d("appState", "false")
        }
    }

    fun pushPendingSteps() {

        if (accumulatedSteps > 0f && isAppActive) {
            Log.d("debug", "pushing steps to DB, accumulated steps = $accumulatedSteps")
            if (taskViewModel?.allHabits?.value?.isNotEmpty() == true) {
                taskViewModel?.updateStepCount(accumulatedSteps.toInt())
                accumulatedSteps = 0f
            } else {
                Log.d("debug", "LiveData is null still")
            }



            Log.d("debug", "pushed steps to DB")
        } else {
            Log.d("debug", "no steps to push")
        }
    }
}