package com.example.mobiledevca_taskapp.taskDatabase

import androidx.annotation.WorkerThread
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val TaskDao: TaskDAO) {

    //Flow notifies observer when data changes
    val allTasks: Flow<List<Task>> = TaskDao.getAllTasks()

    @WorkerThread
    suspend fun insert(task: Task) {
        TaskDao.insertTask(task)
    }

    @WorkerThread
    suspend fun deleteAllTasks() {
        TaskDao.deleteAllTasks()
    }
}