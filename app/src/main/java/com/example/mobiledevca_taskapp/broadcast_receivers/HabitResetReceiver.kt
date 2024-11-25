package com.example.mobiledevca_taskapp.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitRepository

class HabitResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("HabitResetReceiver", "Alarm triggered. Resetting habits.")
        val app= context?.applicationContext as TaskAppApplication
        val factory = TaskViewModelFactory(app, app.applicationScope)
        val taskViewModel = factory.create(TaskViewModel::class.java)

        val resetType = intent?.getIntExtra("RESET_TYPE", 0)
        if (resetType != null) {
            taskViewModel.resetHabits(resetType)
        }
    }
}