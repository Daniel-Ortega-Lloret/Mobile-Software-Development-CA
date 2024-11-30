package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppRepository
import com.example.mobiledevca_taskapp.taskDatabase.dao.ScheduleDAO
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day

class ScheduleRepository(private val scheduleDao: ScheduleDAO) : TaskAppRepository<Day>(scheduleDao) {

    @WorkerThread
    suspend fun getDayById(dayId: Int) : Day? {
        return scheduleDao.getDayById(dayId)
    }

    @WorkerThread
    suspend fun insertDay(day: Day) {
        scheduleDao.insertDay(day)
    }

    @WorkerThread
    fun getAllDaysFromDatabase(): LiveData<List<Day>> {
        return scheduleDao.getAllDays()
    }
}