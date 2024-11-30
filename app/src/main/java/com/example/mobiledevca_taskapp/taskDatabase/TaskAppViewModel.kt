package com.example.mobiledevca_taskapp.taskDatabase

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot
import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskRepository
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitRepository
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.NotificationEvent
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.StepNotificationMaker
import com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskViewModel(application: Application, private val applicationScope: CoroutineScope) : AndroidViewModel(application) {
    private val database = TaskAppRoomDatabase.getDatabase(application, applicationScope)

    private val taskRepository = TaskRepository(database.taskDao())
    private val scheduleRepository = ScheduleRepository(database.scheduleDao())
    private val habitRepository = HabitRepository(database.habitDao())

    private val _stepGoalReached = MutableLiveData<NotificationEvent<Int>>()
    val stepGoalReached: LiveData<NotificationEvent<Int>> get() = _stepGoalReached
    private val _isStepItemAdded = MutableLiveData(false)
    val isStepItemAdded: LiveData<Boolean> get() = _isStepItemAdded
    private var notificationSent = false


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

    fun deleteTask(taskId: Int) = viewModelScope.launch {
        taskRepository.deleteTask(taskId)
    }

    fun ChangeCheckbox(task: Task) = viewModelScope.launch {
        taskRepository.ChangeCheckbox(task)
    }


    private val _allDays = MutableLiveData<List<Day>>()
    val allDays: LiveData<List<Day>> = _allDays

    // Preload the tasks for the week
    fun preLoadWeekTasks() {

        val currentWeek = getCurrentWeek()

        // Mock data to simulate week task loading
        val tasks = listOf(
            Task(0, "Meeting", "Team sync-up", false, "10:00", "02:11:2025"),
            Task(0, "Gym", "Workout", false, "06:00", "09:08:2023")
        )

        val daysOfWeek = currentWeek.mapIndexed { index, dayString ->
            val dayParts = dayString.split(", ")
            val dayName = dayParts[0]
            val dayNumber = dayParts[1].toInt()
            Day(dayId = 0, dayName = dayName, dayNumber = dayNumber, timeSlots = listOf(
                TimeSlot(timeSlotId = 0, time = "06:00", tasks = tasks),
                TimeSlot(timeSlotId = 0, time = "10:00", tasks = tasks)
            ))
        }

        _allDays.value = daysOfWeek
    }

    fun getAllDays() {
        scheduleRepository.getAllDaysFromDatabase().observeForever { days ->
            _allDays.value = days
        }
    }

    fun getCurrentWeek(): List<String> {
        val calendar = Calendar.getInstance()

        // Get the current day of the week
        val today = calendar.get(Calendar.DAY_OF_WEEK)

        // Find the start of the week (Monday)
        calendar.add(Calendar.DAY_OF_WEEK, -today + Calendar.MONDAY)

        val weekDates = mutableListOf<String>()

        // Generate 7 days starting from the current Monday
        val sdf = SimpleDateFormat("EEE, dd", Locale.getDefault())

        for (i in 0 until 7) {
            weekDates.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return weekDates
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

    fun updateHabit(habit: Habit) = viewModelScope.launch {
        habitRepository.updateHabit(habit)
    }

    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        habitRepository.deleteHabit(habit)
    }

    fun getAllHabits() = viewModelScope.launch {
        allHabits
    }

    fun getTotalStepsById(habitId: Int) = viewModelScope.launch {
        habitRepository.getTotalStepsById(habitId)
    }

    fun setStepItemAdded(added:Boolean) {
        _isStepItemAdded.value = added
    }

    fun updateStepCount(currentSteps: Int) = viewModelScope.launch {
        Log.d("debug","updating steps: $currentSteps")
//        Log.d("debug", "habits are: ${allHabits.value}")
        allHabits.value?.forEach { habit ->
            val newCount = habit.habitStepCount?.plus(currentSteps)
            habit.habitTotalStepCount?.let { totalSteps ->
                if (newCount != null) {
                    if(newCount >= totalSteps && !notificationSent) {
                        _stepGoalReached.postValue(NotificationEvent(totalSteps))
                        notificationSent = true
                    }
                }
            }
            Log.d("debug", "habit count is: ${habit.habitStepCount}")
            Log.d("debug", "new count is : $newCount")
            if (newCount != null) {
                habitRepository.updateHabitStepCount(habit.habitId, newCount)
            }
        }
    }

    fun resetHabits(resetType: Int) = viewModelScope.launch {
        val currentTime = Calendar.getInstance()
        Log.d("debug", "Resetting habits for type: $resetType at ${currentTime.time}")
        Log.d("debug", "habits rn: ${allHabits.value}")
        val habitList = habitRepository.getAllHabits()
        habitList.forEach { habit ->
            val resetRequired = when (habit.habitReset) {
                1 -> true
                2 -> currentTime.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                3 -> currentTime.get(Calendar.DAY_OF_MONTH) == 1
                else -> false
            }

            if (resetRequired) {
                Log.d("TaskViewModel", "Resetting habit: ${habit.habitName}")
                habitRepository.updateHabitCount(habit.habitId, 0)
                habitRepository.updateHabitStepCount(habit.habitId, 0)
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