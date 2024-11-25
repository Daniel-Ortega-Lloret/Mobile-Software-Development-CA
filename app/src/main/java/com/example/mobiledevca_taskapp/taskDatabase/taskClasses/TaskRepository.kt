package com.example.mobiledevca_taskapp.taskDatabase.taskClasses

import androidx.annotation.WorkerThread
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppRepository
import com.example.mobiledevca_taskapp.taskDatabase.dao.TaskDAO
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task

class TaskRepository (private val taskDAO: TaskDAO): TaskAppRepository<Task>(taskDAO){
    @WorkerThread
    suspend fun updateTask(task: Task)
    {
        taskDAO.updateTaskById(task.taskId, task.taskName, task.description)
    }
}