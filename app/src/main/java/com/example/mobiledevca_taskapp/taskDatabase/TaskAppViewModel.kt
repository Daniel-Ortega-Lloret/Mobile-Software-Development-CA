package com.example.mobiledevca_taskapp.taskDatabase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(application: Application, private val applicationScope: CoroutineScope) : AndroidViewModel(application) {
    private val database = TaskAppRoomDatabase.getDatabase(application, applicationScope)

    private val taskRepository = TaskAppRepository(database.taskDao())
    private val habitRepository = HabitRepository(database.habitDao())

    //Observer for the tasks repository, only updates UI if data changes
    val allTasks: LiveData<List<Task>> = taskRepository.allItems.asLiveData()

    fun insertTask(task: Task) = viewModelScope.launch {
        taskRepository.insert(task)
    }

    fun deleteAllTasks() = viewModelScope.launch {
        taskRepository.deleteAll()
    }

    val allHabits: LiveData<List<Habit>> = habitRepository.allItems.asLiveData()

    fun insertHabit(habit: Habit) = viewModelScope.launch {
        habitRepository.insert(habit)
    }

    fun deleteAllHabits() = viewModelScope.launch {
        habitRepository.deleteAll()
    }

    fun updateHabitCount(habitId: Int, newCount: Int) = viewModelScope.launch {
        habitRepository.updateHabitCount(habitId, newCount)
    }

    fun getHabitById(habitId: Int) = viewModelScope.launch {
        habitRepository.getHabitById(habitId)
    }

    fun getAllHabits() = viewModelScope.launch {
        allHabits
    }

    fun getTotalStepsById(habitId: Int) = viewModelScope.launch {
        habitRepository.getTotalStepsById(habitId)
    }


    fun updateStepCount(currentSteps: Int) = viewModelScope.launch {
        Log.d("debug","updating steps: $currentSteps")
//        Log.d("debug", "habits are: ${allHabits.value}")
        allHabits.value?.forEach { habit ->
            val newCount = habit.habitStepCount?.plus(currentSteps)
//            Log.d("debug", "habit count is: ${habit.habitStepCount}")
//            Log.d("debug", "new count is : $newCount")
            if (newCount != null) {
                habitRepository.updateHabitStepCount(habit.habitId, newCount)
            }

        }
    }

    fun resetHabits(resetType: Int) = viewModelScope.launch {
        val currentTime = Calendar.getInstance()
        Log.d("debug", "Resetting habits for type: $resetType at ${currentTime.time}")
        allHabits.value?.forEach { habit ->
            val resetRequired = when (habit.habitReset) {
                1 -> true
                2 -> currentTime.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                3 -> currentTime.get(Calendar.DAY_OF_MONTH) == 1
                else -> false
            }

            if (resetRequired) {
                Log.d("TaskViewModel", "Resetting habit: ${habit.habitName}")
                habitRepository.updateHabitCount(habit.habitId, 0)
            }
        }
    }


}

class TaskViewModelFactory(
    private val application: TaskAppApplication,
    private val applicationScope: CoroutineScope
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application, applicationScope) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}