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
import java.sql.Time
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    val allDays: LiveData<List<Day>> get() = _allDays

    private val _selectedDayTasks = MutableLiveData<List<Task>>()
    val selectedDayTasks: LiveData<List<Task>> = _selectedDayTasks

    private var currentWeekStartDate: String = ""

    fun updateTasksForSelectedDay(day: Day) {

        val tasksForSelectedDay = day.timeSlots.flatMap { it.tasks }
        _selectedDayTasks.value = tasksForSelectedDay
    }

    fun ensureStateConsistency() {
        if (currentWeekStartDate.isEmpty()) {
            Log.e("WeekNavigation", "Current week start date is uninitialized!")
            preLoadWeekTasks()
        }
    }

    // Preload the tasks for the week
    fun preLoadWeekTasks() {
        val calendar = Calendar.getInstance()

        val today = Date()
        calendar.time = today

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val startDate = calendar.time
        currentWeekStartDate = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault()).format(startDate)

        val week = List(7) { index ->
            val dayDate = calendar.time
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(dayDate)
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            Day(
                dayId = 0,
                dayName = dayName,
                dayNumber = dayNumber
            )
        }

        _allDays.value = week
    }


    fun loadPreviousWeekTasks() {
        ensureStateConsistency()
        val previousWeekStartDate = getPreviousWeekDate(currentWeekStartDate)
        val previousWeek = getWeekForDate(previousWeekStartDate)

        if (previousWeek.isNotEmpty()) {
            _allDays.value = previousWeek
            currentWeekStartDate = previousWeekStartDate
            Log.d("WeekNavigation", "Updated currentWeekStartDate to: $currentWeekStartDate")
        } else {
            Log.e("WeekNavigation", "Failed to load previous week.")
        }
    }

    private fun isValidDate(date: String): Boolean {
        return try {
            SimpleDateFormat("dd:MM:yyyy", Locale.getDefault()).parse(date) != null
        } catch (e: ParseException) {
            false
        }
    }

    fun getAllDays() {
        scheduleRepository.getAllDaysFromDatabase().observeForever { days ->
            _allDays.value = days
        }
    }

    fun loadNextWeekTasks() {
        ensureStateConsistency()
        val nextWeekStartDate = getNextWeekDate(currentWeekStartDate)
        val nextWeek = getWeekForDate(nextWeekStartDate)

        if (nextWeek.isNotEmpty()) {
            _allDays.value = nextWeek
            currentWeekStartDate = nextWeekStartDate
            Log.d("WeekNavigation", "Updated currentWeekStartDate to: $currentWeekStartDate")
        } else {
            Log.e("WeekNavigation", "Failed to load next week.")
        }
    }

    private fun getNextWeekDate(startDate: String): String {
        if (!isValidDate(startDate)) {
            return ""
        }

        val sdf = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
        val parsedDate = sdf.parse(startDate) ?: throw IllegalArgumentException("Invalid start date")

        val calendar = Calendar.getInstance().apply {
            time = parsedDate
            add(Calendar.DAY_OF_YEAR, 7)
        }
        val monday = calculateMonday(calendar.time)
        return sdf.format(monday)
    }


    private fun getPreviousWeekDate(startDate: String): String {
        if (!isValidDate(startDate)) {
            return ""
        }
        val sdf = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
        val parsedDate = sdf.parse(startDate) ?: throw IllegalArgumentException("Invalid start date")

        val calendar = Calendar.getInstance().apply {
            time = parsedDate
            add(Calendar.DAY_OF_YEAR, -7)
        }
        val monday = calculateMonday(calendar.time)
        return sdf.format(monday)
    }

    private fun getWeekForDate(startDate: String): List<Day> {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())

        calendar.time = sdf.parse(startDate) ?: return emptyList()

        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = (currentDayOfWeek - Calendar.MONDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -diff)

        val week = mutableListOf<Day>()
        for (i in 0 until 7) {
            val date = calendar.time
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
            week.add(Day(
                dayId = 0,
                dayName = dayName,
                dayNumber = dayNumber
            ))

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return week
    }

    private fun calculateMonday(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToMonday = (dayOfWeek - Calendar.MONDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -daysToMonday)
        return calendar.time
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