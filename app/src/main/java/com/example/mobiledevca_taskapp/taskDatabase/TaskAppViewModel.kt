package com.example.mobiledevca_taskapp.taskDatabase

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.example.mobiledevca_taskapp.taskDatabase.entities.DayTask
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot
import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskRepository
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitRepository
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.NotificationEvent
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.StepNotificationMaker
import com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.sql.Time
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class TaskViewModel(application: Application, private val applicationScope: CoroutineScope) : AndroidViewModel(application) {
    private val database = TaskAppRoomDatabase.getDatabase(application, applicationScope)
    val dbExecutor = Executors.newSingleThreadExecutor()
    val dbScope = CoroutineScope(dbExecutor.asCoroutineDispatcher())

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

    fun insertTask(task: Task) = dbScope.launch {
        Log.d("schedule", "inserting task")

        taskRepository.insert(task)

        val newTaskId = taskRepository.getTaskId(task.taskName, task.time, task.date)
        val newTask = task.copy(taskId = newTaskId)
        ensureDayExistsAndInsertTask(task.date, newTask)
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

    private val _selectedTimeSlots = MutableLiveData<List<TimeSlot>>()
    val selectedTimeSlots: LiveData<List<TimeSlot>> = _selectedTimeSlots


    private var currentWeekStartDate: String = ""

    fun updateTasksForSelectedDay(day: Day) = viewModelScope.launch(Dispatchers.IO) {
        Log.d("schedule", "selected day is $day")

        val taskIdsForDay = scheduleRepository.getTaskIdsForDay(day.dayId)
        val tasks = taskRepository.getTasksByIds(taskIdsForDay)

        _selectedDayTasks.postValue(tasks)
    }

    private fun ensureStateConsistency() {
        if (currentWeekStartDate.isEmpty()) {
            Log.e("WeekNavigation", "Current week start date is uninitialized!")
            preLoadWeekTasks()
        }
    }

    // Main function to ensure day exists and insert task
    private fun ensureDayExistsAndInsertTask(date: String, task: Task) {
        Log.d("schedule", "starting to add a day and timeslot")
        viewModelScope.launch(Dispatchers.IO) {
            // Parse the date
            val sdf = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(date) ?: throw IllegalArgumentException("Invalid date format")

            // Extract day details
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 2
            val year = calendar.get(Calendar.YEAR)
            Log.d("schedule", "parsed data is number: $dayNumber\nmonth:$month\nyear:$year\n")
            // Check or create the day
            val day = getOrCreateDay(dayNumber, month, year, task)

            // Associate the task with the day
            insertDayTask(day, task)

            // Refresh time slots for the UI
            updateTimeSlotsForDay(day)
        }
    }

    // Helper function to get or create a Day
    private suspend fun getOrCreateDay(dayNumber: Int, month: Int, year: Int, task: Task): Day {
        var createdDay: Day
        Log.d("schedule", "looking at the day")

        var existingDay = withContext(Dispatchers.IO) {
            scheduleRepository.getDayByDate(dayNumber, month, year)
        }

        createdDay = if (existingDay != null) {
            Log.d("schedule", "existing day is $existingDay")

            updateDayTimeSlot(existingDay, task)
            existingDay
        } else {
            Log.d("schedule", "existing day is null")

            val newTimeSlot = createTimeSlot(task)
            val newDay = createDay(dayNumber, month, year, newTimeSlot)

            withContext(Dispatchers.IO) {
                scheduleRepository.insertDay(newDay)
            }

            newDay
        }

        return createdDay
    }


    // Method to create a new TimeSlot
    private suspend fun createTimeSlot(task: Task): TimeSlot = withContext(Dispatchers.IO) {
        Log.d("schedule", "creating time slot")

        var existingTimeSlot = scheduleRepository.getTimeSlotForTime(task.time)

        if (existingTimeSlot != null) {
            Log.d("schedule", "time slot exists")
            return@withContext existingTimeSlot
        } else {
            val newTimeSlot = TimeSlot(time = task.time, tasks = listOf(task))

            scheduleRepository.insertTimeSlot(newTimeSlot).also {
                Log.d("schedule", "New TimeSlot inserted: $newTimeSlot")
            }

            var newTimeSlotId = scheduleRepository.getTimeSlotId(newTimeSlot.time)

            val createdTimeSlot = newTimeSlot.copy(timeSlotId = newTimeSlotId)

            Log.d("schedule", "created time slot is $createdTimeSlot")

            return@withContext createdTimeSlot
        }
    }



    private suspend fun createDay(dayNumber: Int, month: Int, year: Int, timeSlot: TimeSlot): Day = withContext(Dispatchers.IO) {
        val newDay = Day(
            dayName = getDayName(dayNumber, month, year),
            dayNumber = dayNumber,
            month = month,
            year = year,
            timeSlots = listOf(timeSlot)
        )

        scheduleRepository.insertDay(newDay).also {
            Log.d("schedule", "new timeslot inserted: $newDay")
        }

        var newDayId = scheduleRepository.getDayId(dayNumber, month, year)

        val createdDay = newDay.copy(dayId = newDayId)

        Log.d("schedule", "creating new Day: $createdDay")

        return@withContext createdDay
    }

    private suspend fun updateDayTimeSlot(day: Day, task: Task) = withContext(Dispatchers.IO) {
        Log.d("schedule", "updating time slot for task: $task in day: $day")

        var existingTimeSlot = scheduleRepository.getTimeSlotForTime(task.time)

        if (existingTimeSlot != null) {
            Log.d("schedule", "existing TimeSlot: $existingTimeSlot")

            val updatedTasks = existingTimeSlot.tasks + task
            val updatedTimeSlot = existingTimeSlot.copy(tasks = updatedTasks)

            scheduleRepository.updateTimeSlot(updatedTimeSlot).also {
                Log.d("schedule", "timeslot udpated: $updatedTimeSlot")
            }

            val updatedTimeSlots = day.timeSlots.map {
                if (it.timeSlotId == existingTimeSlot.timeSlotId) updatedTimeSlot else it
            }.ifEmpty { day.timeSlots + updatedTimeSlot } // Ensure inclusion of updatedTimeSlot

            val updatedDay = day.copy(timeSlots = updatedTimeSlots)

            scheduleRepository.updateDay(updatedDay).also {
                Log.d("schedule", "Day updated: $updatedDay")
            }
        } else {
            Log.d("schedule", "making new timeslot cause it doesnt exist")

            val newTimeSlot = createTimeSlot(task)
            val updatedDay = day.copy(timeSlots = day.timeSlots + newTimeSlot)

            scheduleRepository.updateDay(updatedDay).also {
                Log.d("schedule", "timeslot udpated: $updatedDay")
            }
        }
    }

    private suspend fun insertDayTask(day: Day, task: Task) = withContext(Dispatchers.IO) {
        Log.d("schedule", "tried to insert day: $day and task: $task")

        val dayTask = DayTask(dayId = day.dayId, taskId = task.taskId)

        scheduleRepository.insertDayTask(dayTask).also {
            Log.d("schedule", "timeslot udpated: $dayTask")
        }
    }



    private fun updateTimeSlotsForDay(day: Day) {
        viewModelScope.launch(Dispatchers.IO) {
            val timeSlots = scheduleRepository.getTimeSlotsForDay(day.dayId, day.month, day.year)
            _selectedTimeSlots.postValue(timeSlots)
        }
    }

    private fun getDayName(dayNumber: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance()

        calendar.set(year, month - 1, dayNumber)

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"
            else -> "Unknown"
        }
    }

    fun preLoadWeekTasks() {
        val calendar = Calendar.getInstance()

        val today = Date()
        calendar.time = today

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val startDate = calendar.time
        currentWeekStartDate = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault()).format(startDate)

        val week = mutableListOf<Day>()

        for (index in 0 until 7) {
            val dayDate = calendar.time
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(dayDate)
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            val day = Day(
                dayId = 0,  // Placeholder dayId
                dayName = dayName,
                dayNumber = dayNumber,
                timeSlots = emptyList(),
                month = month,
                year = year
            )

            week.add(day)
        }

        _allDays.postValue(week)
    }

    fun updateTimeSlotsForSelectedDay(day: Day) = viewModelScope.launch(Dispatchers.IO){
        val taskIdsForDay = scheduleRepository.getTaskIdsForDay(day.dayId)

        val tasksForDay = taskRepository.getTasksByIds(taskIdsForDay)

        val timeSlots = convertTasksToTimeSlots(tasksForDay)

        _selectedTimeSlots.postValue(timeSlots)
    }

    private fun convertTasksToTimeSlots(tasks: List<Task>): List<TimeSlot> {
        val allTimeSlots = (0 until 24).map { hour ->
            val hourString = String.format("%02d:00", hour)
            val tasksForHour = tasks.filter { it.time.startsWith(hourString) }

            TimeSlot(timeSlotId = hour, time = hourString, tasks = tasksForHour)
        }
        return allTimeSlots
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
            Log.d("WeekNavigation", "updated currentWeekStartDate to: $currentWeekStartDate")
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

    fun getWeekForDate(startDate: String): List<Day> {
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
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val day = Day(
                dayId = 0,
                dayName = dayName,
                dayNumber = dayNumber,
                timeSlots = emptyList(),
                month = month,
                year = year
            )

            week.add(day)
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

    override fun onCleared() {
        super.onCleared()
        dbExecutor.shutdown()
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