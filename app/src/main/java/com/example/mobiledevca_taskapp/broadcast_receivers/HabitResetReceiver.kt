package com.example.mobiledevca_taskapp.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory

class HabitResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val app= context?.applicationContext as TaskAppApplication
        val factory = TaskViewModelFactory(app, app.applicationScope)
        val taskViewModel = factory.create(TaskViewModel::class.java)

        val resetType = intent?.getIntExtra("RESET_TYPE", 0)
        if (resetType != null) {
            taskViewModel.resetHabits(resetType)
        }
    }
}