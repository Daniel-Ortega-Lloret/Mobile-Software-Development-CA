//
package com.example.mobiledevca_taskapp.background

import android.icu.util.Calendar
import android.os.Bundle
import android.os.Message
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitRepository

class HabitResetThread(handler: ThreadHandler, private val taskViewModel: TaskViewModel) : Thread() {

    private var _handler: ThreadHandler = handler

    override fun run() {
        val currentTime = Calendar.getInstance()

        val habits = taskViewModel.allHabits.value ?: emptyList()

        habits.forEach { habit ->
            var resetDone = false

            when (habit.habitReset) {
                1 -> {
                    if (currentTime.get(Calendar.HOUR_OF_DAY) == 0) {
                        resetHabit(habit)
                        resetDone = true
                    }
                }
                2 -> {
                    if (currentTime.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && currentTime.get(Calendar.HOUR_OF_DAY) == 0) {
                        resetHabit(habit)
                        resetDone = true
                    }
                }
                3 -> {
                    if (currentTime.get(Calendar.DAY_OF_MONTH) == 1 && currentTime.get(Calendar.HOUR_OF_DAY) == 0) {
                        resetHabit(habit)
                        resetDone = true
                    }
                }
            }

            if (resetDone) {
                val message = _handler.obtainMessage().apply {
                    obj = "Habit '${habit.habitName}' reset to 0"
                }
                _handler.sendMessage(message)
            }
        }
    }

    private fun resetHabit(habit: Habit) {
        taskViewModel.updateHabitCount(habit.habitId, 0)
    }
}