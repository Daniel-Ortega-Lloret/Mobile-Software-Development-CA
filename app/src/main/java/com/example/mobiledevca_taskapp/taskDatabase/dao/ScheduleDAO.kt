package com.example.mobiledevca_taskapp.taskDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDAO: BaseDAO<Day> {
    @Query("SELECT * FROM Day")
    override fun getAll(): Flow<List<Day>>

    @Query("DELETE FROM Day")
    override suspend fun deleteAll()

    @Query("SELECT * FROM Day WHERE dayId = :dayId")
    suspend fun getDayById(dayId: Int) : Day?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: Day)

    @Query("SELECT * FROM Day")
    fun getAllDays(): LiveData<List<Day>>

}