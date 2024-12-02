package com.example.mobiledevca_taskapp.taskDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao //Data Access Object pattern for accessing DB
interface TaskDAO : BaseDAO<Task>{
    @Query("SELECT * FROM Task ORDER BY position ASC, taskId DESC")
    override fun getAll(): Flow<List<Task>>

    @Query ("UPDATE Task SET position = :task_Position WHERE taskId = :task_Id")
    suspend fun updateTaskPositionById(task_Id: Int, task_Position: Int)

    @Query("DELETE FROM Task")
    override suspend fun deleteAll()

    @Query("UPDATE Task SET taskName = :task_Name, description = :task_Description, time = :task_Time, date = :task_Date WHERE taskId = :task_Id")
    suspend fun updateTaskById(task_Id: Int, task_Name: String, task_Description: String, task_Time: String, task_Date: String)

    @Query("DELETE FROM Task WHERE taskId = :task_Id")
    suspend fun deleteTaskById(task_Id: Int)

    // For Changing The Checkbox
    @Query("UPDATE Task Set isChecked = :is_Checked WHERE taskId = :task_Id")
    suspend fun changeTaskById(task_Id: Int, is_Checked: Boolean)
}