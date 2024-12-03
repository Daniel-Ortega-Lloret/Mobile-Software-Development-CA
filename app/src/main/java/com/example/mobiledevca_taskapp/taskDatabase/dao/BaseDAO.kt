package com.example.mobiledevca_taskapp.taskDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BaseDAO<T> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: T)

    @Query("DELETE FROM Task")
    suspend fun deleteAll()

    @Query("SELECT * FROM Task")
    fun getAll(): Flow<List<T>>
}