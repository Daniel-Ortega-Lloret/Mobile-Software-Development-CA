package com.example.mobiledevca_taskapp.taskDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.DayTask
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDAO: BaseDAO<Day> {
    @Query("SELECT * FROM Day")
    override fun getAll(): Flow<List<Day>>

    @Query("DELETE FROM Day")
    override suspend fun deleteAll()

    @Query("SELECT * FROM Day WHERE dayId = :dayId")
    suspend fun getDayById(dayId: Int) : Day?

    @Transaction
    suspend fun insertDayWithTimeSlot(day:Day, timeSlot: TimeSlot) {
        insertTimeSlot(timeSlot)
        insertDay(day)
    }

    @Query("SELECT * FROM Day")
    fun getAllDays(): LiveData<List<Day>>

    @Query("SELECT * FROM Day WHERE dayNumber = :dayNumber AND month = :month AND year = :year")
    suspend fun getDayByDate(dayNumber: Int, month: Int, year: Int): Day?

    @Query("SELECT EXISTS( SELECT 1 FROM DayTask WHERE dayId = :dayId)")
    suspend fun doesDayHaveTasks(dayId: Long): Boolean

    @Delete
    suspend fun deleteDayTask(dayTask: DayTask)

    @Query("SELECT * FROM TimeSlot WHERE time = :time")
    suspend fun getTimeSlotByTime(time: String): TimeSlot?

    @Transaction
    suspend fun getTimeSlotsForDay(dayNumber: Int, month: Int, year: Int): List<TimeSlot> {
        val day = getDayByDate(dayNumber, month, year)
        return day?.timeSlots ?: emptyList()
    }

    @Query("SELECT * FROM TimeSlot WHERE timeSlotId = :timeSlotId")
    suspend fun getTimeSlotById(timeSlotId:Int): TimeSlot?

    @Query("SELECT taskId FROM DayTask WHERE dayId = :dayId")
    suspend fun getTaskIdsForDay(dayId: Int): List<Int>

    @Update
    suspend fun updateTimeSlot(timeSlot: TimeSlot)

    @Query("SELECT dayId FROM Day WHERE dayNumber = :dayNumber AND month = :month AND year = :year")
    suspend fun getDayId(dayNumber: Int, month: Int, year: Int) : Int

    @Query("SELECT timeSlotId FROM TimeSlot WHERE time = :time")
    suspend fun getTimeSlotId(time:String) : Int

    @Query("SELECT * FROM TimeSlot WHERE time = :time LIMIT 1")
    suspend fun getTimeSlotForTime(time: String): TimeSlot?

    @Update
    suspend fun updateDay(day: Day)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: Day): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTimeSlot(timeSlot: TimeSlot): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDayTask(dayTask: DayTask)

    @Query("SELECT * FROM Day WHERE dayNumber IN (:days) AND month IN (:months) AND year IN (:years)")
    suspend fun getDaysByDates(days: List<Int>, months: List<Int>, years: List<Int>): List<Day>

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlot)

    @Query("SELECT dayId FROM DayTask WHERE taskId = :taskId")
    suspend fun getDaysForTask(taskId: Int): List<Int>

    @Delete
    suspend fun deleteDay(day: Day)

    @Query ("SELECT * FROM Day")
    suspend fun getAllStaticDays() : List<Day>
}