package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

import androidx.annotation.WorkerThread
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppRepository
import com.example.mobiledevca_taskapp.taskDatabase.dao.HabitDAO
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import kotlinx.coroutines.flow.forEach

class HabitRepository(private val habitDao: HabitDAO) : TaskAppRepository<Habit>(habitDao){
    @WorkerThread
    suspend fun updateHabitCount(habitId: Int, newCount: Int) {
        habitDao.updateHabitCount(habitId, newCount)
    }

    @WorkerThread
    suspend fun getHabitById(habitId: Int): Habit? {
        return habitDao.getHabitById(habitId)
    }

    @WorkerThread
    suspend fun getTotalStepsById(habitId: Int) : Int? {
        return habitDao.getTotalStepsById(habitId)
    }

    @WorkerThread
    suspend fun updateHabitStepCount(habitId: Int, newCount: Int) {
        habitDao.updateHabitStepCount(habitId, newCount)
    }
}