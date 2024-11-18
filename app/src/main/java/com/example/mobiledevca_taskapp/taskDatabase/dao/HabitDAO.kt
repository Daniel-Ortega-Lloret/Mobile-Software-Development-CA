package com.example.mobiledevca_taskapp.taskDatabase.dao

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
}