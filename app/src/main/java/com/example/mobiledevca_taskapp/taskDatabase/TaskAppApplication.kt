package com.example.mobiledevca_taskapp.taskDatabase

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.mobiledevca_taskapp.broadcast_receivers.HabitResetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.Calendar

class TaskAppApplication: Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    //by lazy makes it so that these are only made when they are needed, not as the activity starts
    val database by lazy { TaskAppRoomDatabase.getDatabase(this, applicationScope) }
    val taskRepository by lazy { TaskAppRepository(database.taskDao()) }
    val habitRepository by lazy { TaskAppRepository(database.habitDao())}

    override fun onCreate() {
        super.onCreate()

        scheduleResetAlarm()
    }

    private fun scheduleResetAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, HabitResetReceiver::class.java)

        intent.putExtra("RESET_TYPE", 1)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0) //midnight
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        //Schedule alarm for tomorrow if time has already passed today
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        //24 hour alarm
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            60*1000L,
            pendingIntent
        )

        val weeklyCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (weeklyCalendar.timeInMillis < System.currentTimeMillis()) {
            weeklyCalendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        //Weekly alarm
        intent.putExtra("RESET_TYPE", 2)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY*7,
            pendingIntent
        )

        val monthlyCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)  // Set to the 1st day of the next month
            add(Calendar.MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)  // Set to midnight
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

        }

        if (monthlyCalendar.timeInMillis < System.currentTimeMillis()) {
            monthlyCalendar.add(Calendar.MONTH, 1)
        }

        //Monthly alarm
        intent.putExtra("RESET_TYPE", 3)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 30,
            pendingIntent
        )
    }
}