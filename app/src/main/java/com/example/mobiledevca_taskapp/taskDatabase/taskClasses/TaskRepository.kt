package com.example.mobiledevca_taskapp.taskDatabase.taskClasses

import androidx.annotation.WorkerThread
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppRepository
import com.example.mobiledevca_taskapp.taskDatabase.dao.TaskDAO
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task

class TaskRepository (private val taskDAO: TaskDAO): TaskAppRepository<Task>(taskDAO){
    @WorkerThread
    suspend fun updateTask(task: Task)
    {
        taskDAO.updateTaskById(task.taskId, task.taskName, task.description, task.time, task.date)
    }

    @WorkerThread
    suspend fun deleteTask(taskId: Int){
        taskDAO.deleteTaskById(taskId)
    }

    @WorkerThread
    suspend fun ChangeCheckbox(task: Task)
    {
        taskDAO.changeTaskById(task.taskId, task.isChecked)
    }

    @WorkerThread
    suspend fun getTasksForDay(taskId: Int) : List<Task>{
        return taskDAO.getTasksForDay(taskId)
    }

    @WorkerThread
    fun getTasksByIds(taskIds: List<Int>): List<Task> {
        return taskDAO.getTasksByIds(taskIds)
    }

    @WorkerThread
    suspend fun getTaskId(taskName: String, time: String, date: String): Int{
        return taskDAO.getTaskId(taskName,time,date)
    }
}