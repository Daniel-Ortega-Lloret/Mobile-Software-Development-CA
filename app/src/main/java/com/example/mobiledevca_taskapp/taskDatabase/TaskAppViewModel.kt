package com.example.mobiledevca_taskapp.taskDatabase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TaskViewModel(application: Application, private val applicationScope: CoroutineScope) : AndroidViewModel(application) {
    private val database = TaskAppRoomDatabase.getDatabase(application, applicationScope)

    private val taskRepository = TaskRepository(database.taskDao())
    private val habitRepository = TaskAppRepository(database.habitDao())

    //Observer for the tasks repository, only updates UI if data changes
    val allTasks: LiveData<List<Task>> = taskRepository.allItems.asLiveData()

    fun insertTask(task: Task) = viewModelScope.launch {
        taskRepository.insert(task)

    }

    fun deleteAllTasks() = viewModelScope.launch {
        taskRepository.deleteAll()
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
    }

    val allHabits: LiveData<List<Habit>> = habitRepository.allItems.asLiveData()

    fun insertHabit(habit: Habit) = viewModelScope.launch {
        habitRepository.insert(habit)
    }

    fun deleteAllHabits() = viewModelScope.launch {
        habitRepository.deleteAll()
    }

}

class TaskViewModelFactory(
    private val application: TaskAppApplication,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application, application.applicationScope) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}