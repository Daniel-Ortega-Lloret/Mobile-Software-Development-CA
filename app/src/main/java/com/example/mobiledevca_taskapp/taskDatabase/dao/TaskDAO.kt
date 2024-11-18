package com.example.mobiledevca_taskapp.taskDatabase.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao //Data Access Object pattern for accessing DB
interface TaskDAO : BaseDAO<Task>{
    @Query("SELECT * FROM Task")
    override fun getAll(): Flow<List<Task>>

    @Query("DELETE FROM Task")
    override suspend fun deleteAll()
}