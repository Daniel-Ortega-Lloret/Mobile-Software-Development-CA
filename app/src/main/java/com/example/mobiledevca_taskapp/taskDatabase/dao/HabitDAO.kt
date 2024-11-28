package com.example.mobiledevca_taskapp.taskDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDAO : BaseDAO<Habit>{
    @Query("SELECT * FROM Habit")
    override fun getAll(): Flow<List<Habit>>

    @Query("DELETE FROM Habit")
    override suspend fun deleteAll()

    @Query("UPDATE Habit SET habitCount = :newCount WHERE habitId = :habitId")
    suspend fun updateHabitCount(habitId: Int, newCount: Int)

    @Query("SELECT * FROM Habit WHERE habitId = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: Int): Habit?

    @Query("SELECT habitTotalStepCount FROM Habit WHERE habitId = :habitId")
    suspend fun getTotalStepsById(habitId: Int): Int?

    @Query("UPDATE Habit SET habitStepCount = :newCount WHERE habitId = :habitId")
    suspend fun updateHabitStepCount(habitId: Int, newCount: Int)

    @Query("SELECT * FROM Habit")
    suspend fun getAllHabits(): List<Habit>
}