package com.example.mobiledevca_taskapp.taskDatabase.dao

import androidx.lifecycle.LiveData
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

    @Query("UPDATE Task SET taskName = :task_Name, description = :task_Description WHERE taskId = :task_Id")
    suspend fun updateTaskById(task_Id: Int, task_Name: String, task_Description: String)

    @Query("DELETE FROM Task WHERE taskId = :task_Id")
    suspend fun deleteTaskById(task_Id: Int)

    @Query("UPDATE Task Set isChecked = :is_Checked WHERE taskId = :task_Id")
    suspend fun changeTaskById(task_Id: Int, is_Checked: Boolean)
}