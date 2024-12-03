package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppRepository
import com.example.mobiledevca_taskapp.taskDatabase.dao.ScheduleDAO
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.DayTask
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot

class ScheduleRepository(private val scheduleDao: ScheduleDAO) : TaskAppRepository<Day>(scheduleDao) {

    @WorkerThread
    suspend fun getDayById(dayId: Int) : Day? {
        Log.d("schedule", "gettin da day through da IDDDD")
        return scheduleDao.getDayById(dayId)
    }

    @WorkerThread
    suspend fun getDayId(dayNumber: Int, month: Int, year: Int): Int {
        Log.d("schedule", "gettin da day ID")
        return scheduleDao.getDayId(dayNumber,month,year)
    }

    @WorkerThread
    suspend fun getTaskIdsForDay(dayId: Int): List<Int> {
        return scheduleDao.getTaskIdsForDay(dayId)
    }

    @WorkerThread
    suspend fun insertDay(day: Day) {
        scheduleDao.insertDay(day)
    }

    @WorkerThread
    suspend fun insertTimeSlot(timeSlot: TimeSlot) {
        val existingSlot = scheduleDao.getTimeSlotByTime(timeSlot.time)

        if (existingSlot == null) {
            Log.d("schedule", "timeslot doesnt exist")
            scheduleDao.insertTimeSlot(timeSlot)
        } else {
            Log.d("schedule", "timeslot exists")
            val updatedTasks = existingSlot.tasks + timeSlot.tasks
            val updatedTimeSlot = existingSlot.copy(tasks = updatedTasks)

            scheduleDao.updateTimeSlot(updatedTimeSlot)
        }
    }

    @WorkerThread
    fun getAllDaysFromDatabase(): LiveData<List<Day>> {
        return scheduleDao.getAllDays()
    }

    @WorkerThread
    suspend fun insertDayTask(dayTask: DayTask){
        scheduleDao.insertDayTask(dayTask)
    }

    @WorkerThread
    suspend fun getTimeSlotsForDay(dayNumber: Int, month: Int, year: Int): List<TimeSlot>{
        return scheduleDao.getTimeSlotsForDay(dayNumber, month, year)
    }

    @WorkerThread
    suspend fun getDayByDate(dayNumber: Int, month: Int, year: Int): Day? {
        Log.d("schedule", "Running query for dayNumber=$dayNumber, month=$month, year=$year")
        return scheduleDao.getDayByDate(dayNumber,month,year)
    }

    @WorkerThread
    suspend fun updateTimeSlot(timeSlot: TimeSlot) {
        scheduleDao.updateTimeSlot(timeSlot)
    }

    @WorkerThread
    suspend fun getTimeSlotId(time : String) : Int {
        return scheduleDao.getTimeSlotId(time)
    }

    @WorkerThread
    suspend fun getTimeSlotForTime(time: String): TimeSlot? {
        return scheduleDao.getTimeSlotForTime(time)
    }

    @WorkerThread
    suspend fun updateDay(day: Day) {
        scheduleDao.updateDay(day)
    }

    @WorkerThread
    suspend fun getDaysByDates(dates: List<Triple<Int, Int, Int>>): List<Day> {
        val days = dates.map { it.first }
        val months = dates.map { it.second }
        val years = dates.map { it.third }
        return scheduleDao.getDaysByDates(days, months, years)
    }

    @WorkerThread
    suspend fun getDaysForTask(taskId: Int): List<Int> {
        return scheduleDao.getDaysForTask(taskId)
    }

    @WorkerThread
    suspend fun deleteTimeSlot(timeSlot: TimeSlot) {
        scheduleDao.deleteTimeSlot(timeSlot)
    }

    @WorkerThread
    suspend fun deleteDay(day: Day) {
        scheduleDao.deleteDay(day)
    }

    @WorkerThread
    suspend fun getAllDays():List<Day> {
        return scheduleDao.getAllStaticDays()
    }
}