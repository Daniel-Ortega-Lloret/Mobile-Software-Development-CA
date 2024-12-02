package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

import androidx.annotation.WorkerThread
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppRepository
import com.example.mobiledevca_taskapp.taskDatabase.dao.HabitDAO
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import kotlinx.coroutines.flow.forEach

class HabitRepository(private val habitDao: HabitDAO) : TaskAppRepository<Habit>(habitDao){
    @WorkerThread
    suspend fun updateHabitOrder(List: List<Habit>)
    {
        // Set Each Individual Tasks Position Correctly
        List.forEach { habit: Habit ->
            habitDao.updateHabitPositionById(habit.habitId, habit.position)
        }
    }


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

    @WorkerThread
    suspend fun getAllHabits() : List<Habit>{
        return habitDao.getAllHabits()
    }

    @WorkerThread
    suspend fun updateHabit(habit: Habit) {
        habit.habitReset?.let {
            habit.habitCountCheck?.let { it1 ->
                habit.habitTotalStepCount?.let { it2 ->
                    habitDao.updateHabit(habit.habitId, habit.habitName,
                        it, it1, it2
                    )
                }
            }
        }
    }

    @WorkerThread
    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit.habitId)
    }
}