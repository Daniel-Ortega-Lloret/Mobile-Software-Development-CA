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
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.DayTask
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot
import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskRepository
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitRepository
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.NotificationEvent
import com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
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


    private val _allDays = MutableLiveData<List<Day>>()
    val allDays: LiveData<List<Day>> get() = _allDays

    private val _selectedDayTasks = MutableLiveData<List<Task>>()
    val selectedDayTasks: LiveData<List<Task>> = _selectedDayTasks
    //Observer for the tasks repository, only updates UI if data changes
    val allTasks: LiveData<List<Task>> = taskRepository.allItems.asLiveData()

    private val _selectedTimeSlots = MutableLiveData<List<TimeSlot>>()
    val selectedTimeSlots: LiveData<List<TimeSlot>> = _selectedTimeSlots

    private var currentWeekStartDate: String = ""
    private var selectedDay: String = ""

    fun updateOrder(newOrder: List<Task>) = viewModelScope.launch {
        taskRepository.updateOrder(newOrder)
    }

    fun insertTask(task: Task) = dbScope.launch {
        Log.d("schedule", "inserting task")
        if (task.date != "null:null:null" && task.time != "null:null")
        {
            taskRepository.insert(task)

            val newTaskId = taskRepository.getTaskId(task.taskName, task.time, task.date)
            val newTask = task.copy(taskId = newTaskId)
            ensureDayExistsAndInsertTask(task.date, newTask)
        }
        else
        {
            taskRepository.insert(task)
        }
    }

    fun deleteAllTasks() = viewModelScope.launch {
        taskRepository.deleteAll()
    }

    fun updateTask(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        runBlocking {
            if (task.date != "null:null:null" && task.time != "null:null")
            {
                var currentTask = taskRepository.getTaskById(task.taskId)
                Log.d("schedule", "old task data is $currentTask")

                if (currentTask?.time != task.time) {
                    val oldTimeSlot = currentTask?.let { scheduleRepository.getTimeSlotForTime(it.time) }
                    if (oldTimeSlot != null) {
                        val updatedTasksInOldTimeSlot = oldTimeSlot.tasks.filter { it.taskId != task.taskId }
                        if (updatedTasksInOldTimeSlot.isEmpty()) {
                            scheduleRepository.deleteTimeSlot(oldTimeSlot)
                        } else {
                            scheduleRepository.updateTimeSlot(oldTimeSlot.copy(tasks = updatedTasksInOldTimeSlot))
                        }
                    }

                    val newTimeSlot = updateCreateTimeSlot(task)

                    val (newDayNumber, newMonth, newYear) = parseDate(task.date)
                    val correctedMonth = newMonth + 1
                    Log.d("schedule", "new date is ${task.date}")
                    if (currentTask != null) {
                        if (currentTask.date == task.date) {

                            Log.d("schedule", "date not changed")
                            val existingDay = scheduleRepository.getDayByDate(newDayNumber, correctedMonth, newYear)

                            if (existingDay != null) {
                                val updatedTimeSlots = existingDay.timeSlots.filter { it.time != currentTask.time }
                                scheduleRepository.updateDay(existingDay.copy(timeSlots = updatedTimeSlots + newTimeSlot))
                            } else {
                                val newDay = Day(dayName = getDayName(newDayNumber, correctedMonth, newYear), dayNumber = newDayNumber, month = correctedMonth, year = newYear, timeSlots = listOf(newTimeSlot))
                                scheduleRepository.insertDay(newDay)
                            }
                        } else {
                            Log.d("schedule", "date changed")

                            val (oldDayNumber, oldMonth, oldYear) = parseDate(currentTask.date)
                            val oldCorrectedMonth = oldMonth + 1
                            val oldDay = scheduleRepository.getDayByDate(oldDayNumber, oldCorrectedMonth, oldYear)
                            Log.d("schedule", "old day is $oldDay")
                            if (oldDay != null) {
                                val updatedTimeSlots = oldDay.timeSlots.filter { it.time != currentTask.time }
                                if (updatedTimeSlots.isEmpty()) {
                                    scheduleRepository.deleteDay(oldDay)
                                } else {
                                    scheduleRepository.updateDay(oldDay.copy(timeSlots = updatedTimeSlots))
                                }
                            }

                            val newDay = Day(dayName = getDayName(newDayNumber, correctedMonth, newYear), dayNumber = newDayNumber, month = correctedMonth, year = newYear)

                            val updatedTimeSlot = if (scheduleRepository.getTimeSlotForTime(task.time) != null) {
                                val existingTimeSlot = scheduleRepository.getTimeSlotForTime(task.time)!!
                                existingTimeSlot.copy(tasks = existingTimeSlot.tasks + task)
                            } else {
                                TimeSlot(time = task.time, tasks = listOf(task))
                            }

                            scheduleRepository.insertDay(newDay.copy(timeSlots = listOf(updatedTimeSlot)))
                        }
                    }

                    taskRepository.updateTask(task.copy(time = newTimeSlot.time))

                    val createdDayId = scheduleRepository.getDayId(newDayNumber, correctedMonth, newYear)
                    val createdDay = scheduleRepository.getDayById(createdDayId)
                    if (createdDay != null){
                        val resultDay = createdDay.copy(dayId = createdDayId)
                        insertDayTask(resultDay, task)
                    }

                } else {
                    Log.d("schedule", "time did not change")

                    val (newDayNumber, newMonth, newYear) = parseDate(task.date)
                    val correctedMonth = newMonth + 1

                    if (currentTask.date != task.date) {
                        Log.d("schedule", "date changed")

                        val (oldDayNumber, oldMonth, oldYear) = parseDate(currentTask.date)
                        val oldCorrectedMonth = oldMonth + 1
                        val oldDay = scheduleRepository.getDayByDate(oldDayNumber, oldCorrectedMonth, oldYear)
                        if (oldDay != null) {
                            val updatedTimeSlots = oldDay.timeSlots.filter { it.time != currentTask.time }
                            if (updatedTimeSlots.isEmpty()) {
                                scheduleRepository.deleteDay(oldDay)
                            } else {
                                scheduleRepository.updateDay(oldDay.copy(timeSlots = updatedTimeSlots))
                            }
                        }

                        val newDay = scheduleRepository.getDayByDate(newDayNumber, correctedMonth, newYear)
                        if (newDay != null) {
                            val existingTimeSlot = scheduleRepository.getTimeSlotForTime(task.time)
                            val updatedTimeSlot = existingTimeSlot?.copy(tasks = existingTimeSlot.tasks + task)
                                ?: TimeSlot(time = task.time, tasks = listOf(task))
                            scheduleRepository.updateDay(newDay.copy(timeSlots = newDay.timeSlots + updatedTimeSlot))
                        } else {
                            val newTimeSlot = TimeSlot(time = task.time, tasks = listOf(task))
                            val createdDay = Day(
                                dayName = getDayName(newDayNumber, correctedMonth, newYear),
                                dayNumber = newDayNumber,
                                month = correctedMonth,
                                year = newYear,
                                timeSlots = listOf(newTimeSlot)
                            )
                            scheduleRepository.insertDay(createdDay)
                        }
                    }

                    val currentTimeSlot = scheduleRepository.getTimeSlotForTime(currentTask.time)
                    if (currentTimeSlot != null) {
                        val updatedTasksInCurrentTimeSlot = currentTimeSlot.tasks.map {
                            if (it.taskId == task.taskId) task else it
                        }
                        scheduleRepository.updateTimeSlot(currentTimeSlot.copy(tasks = updatedTasksInCurrentTimeSlot))
                    }

                    taskRepository.updateTask(task)

                    val createdDayId = scheduleRepository.getDayId(newDayNumber, correctedMonth, newYear)
                    val createdDay = scheduleRepository.getDayById(createdDayId)
                    if (createdDay != null){
                        val resultDay = createdDay.copy(dayId = createdDayId)
                        insertDayTask(resultDay, task)
                    }
                }
            }
            else
            {
                taskRepository.updateTask(task)
            }

        }
    }

    private suspend fun updateCreateTimeSlot(task: Task): TimeSlot {
        Log.d("schedule", "in the timeslot creation/update")
        var existingTimeSlot: TimeSlot? = null

        withContext(Dispatchers.IO){
            val receivedTimeSlot = scheduleRepository.getTimeSlotForTime(task.time)
            if (receivedTimeSlot != null) {
                existingTimeSlot = receivedTimeSlot
            }
        }

        return if (existingTimeSlot == null) {
            // If no existing time slot, create a new one
            val newTimeSlot = TimeSlot(time = task.time, tasks = listOf(task))
            scheduleRepository.insertTimeSlot(newTimeSlot)
            val newTimeSlotId = scheduleRepository.getTimeSlotId(newTimeSlot.time)
            val resultTimeSlot = newTimeSlot.copy(timeSlotId = newTimeSlotId)
            resultTimeSlot
        } else {

            existingTimeSlot!!.copy(tasks = existingTimeSlot!!.tasks + task)

        }
    }

    fun parseDate(date: String): Triple<Int, Int, Int> {
        Log.d("insert","Date String is, $date" )
        if (date != "null:null:null")
        {
            val dateParts = date.split(":")
            val day = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val year = dateParts[2].toInt()
            return Triple(day, month, year)
        }
        return Triple(0, 0, 0)

    }

    fun deleteTask(taskId: Int) = viewModelScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.IO) {
            var task = taskRepository.getTaskById(taskId)
            if (task != null) {

                runBlocking {
                    if (task.date != "null:null:null" && task.time != "null:null") {
                        val affectedDayIds = scheduleRepository.getDaysForTask(taskId)
                        Log.d(
                            "schedule",
                            "updated with new day, lists look like this ${allDays.value}\n${selectedTimeSlots.value}\n${selectedDayTasks.value}"
                        )

                        for (dayId in affectedDayIds) {
                            val day = scheduleRepository.getDayById(dayId)
                            if (day != null) {
                                val updatedTimeSlots = mutableListOf<TimeSlot>()

                                for (timeSlot in day.timeSlots) {
                                    val remainingTasks =
                                        timeSlot.tasks.filter { it.taskId != taskId }

                                    if (remainingTasks.isEmpty()) {
                                        // Delete empty TimeSlot
                                        Log.d("schedule", "timeslot $remainingTasks is empty")

                                        scheduleRepository.deleteTimeSlot(timeSlot)
                                    } else {
                                        // Update TimeSlot with remaining tasks
                                        Log.d("schedule", "timeslot is not empty, $timeSlot")
                                        val updatedTimeSlot = timeSlot.copy(tasks = remainingTasks)
                                        scheduleRepository.updateTimeSlot(updatedTimeSlot)
                                        updatedTimeSlots.add(updatedTimeSlot)
                                    }
                                }

                                val updatedDay = day.copy(timeSlots = updatedTimeSlots)

                                if (updatedTimeSlots.isEmpty()) {
                                    Log.d(
                                        "schedule",
                                        "updating empty day with timeslots of $updatedDay"
                                    )
                                    scheduleRepository.deleteDay(updatedDay)

                                    // Post the new list of days after deletion
                                    val allDays = scheduleRepository.getAllDays()
                                    _allDays.postValue(allDays)
                                } else {
                                    Log.d("schedule", "udpating day with timeslots of$updatedDay")
                                    scheduleRepository.updateDay(updatedDay)
                                    updateTimeSlotsForDay(updatedDay)
                                    updateTasksForSelectedDay(updatedDay)
                                    updateTimeSlotsForSelectedDay(updatedDay)

                                    // Post the new list of days after update
                                    val allDays = scheduleRepository.getAllDays()
                                    _allDays.postValue(allDays)
                                }
                            }
                        }
                        taskRepository.deleteTask(taskId)
                    }
                    else
                    {
                        taskRepository.deleteTask(taskId)
                    }
                    Log.d(
                        "schedule",
                        "updated with new day, lists look like this ${allDays.value}\n${selectedTimeSlots.value}\n${selectedDayTasks.value}"
                    )



                }
            }

        }
    }

    fun ChangeCheckbox(task: Task) = viewModelScope.launch {
        taskRepository.ChangeCheckbox(task)
    }

    fun updateTasksForSelectedDay(day: Day) = viewModelScope.launch(Dispatchers.IO) {
//        Log.d("schedule", "selected day is $day")
        selectedDay = day.dayNumber.toString() + ":" + day.month.toString() + ":" + day.year.toString()
        val taskIdsForDay = scheduleRepository.getTaskIdsForDay(day.dayId)
//        Log.d("schedule", "task ids for this day are $taskIdsForDay")
        val tasks = taskRepository.getTasksByIds(taskIdsForDay)

        _selectedDayTasks.postValue(tasks)
    }

    // Main function to ensure day exists and insert task
    private fun ensureDayExistsAndInsertTask(date: String, task: Task) {
        Log.d("schedule", "starting to add a day and timeslot")
        viewModelScope.launch(Dispatchers.IO) {
            if (date != "null:null:null" && date != "Blank")
            {
                // Parse the date
                val sdf = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.time = sdf.parse(date) ?: throw IllegalArgumentException("Invalid date format")

                val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH) + 2
                val year = calendar.get(Calendar.YEAR)
                Log.d("schedule", "parsed data is number: $dayNumber\nmonth:$month\nyear:$year\n")

                runBlocking {
                    val day = getOrCreateDay(dayNumber, month, year, task)

                    insertDayTask(day, task)

                    updateTimeSlotsForDay(day)

                    Log.d("schedule", "updated with new day, lists look like this ${allDays.value}\n${selectedTimeSlots.value}\n${selectedDayTasks.value}")
                }
            }

        }
    }

    private suspend fun getOrCreateDay(dayNumber: Int, month: Int, year: Int, task: Task): Day {
        var createdDay: Day
        Log.d("schedule", "looking at the day")

        withContext(Dispatchers.IO){
            runBlocking {
                var existingDay = scheduleRepository.getDayByDate(dayNumber, month, year)

                createdDay = if (existingDay != null) {
                    Log.d("schedule", "existing day is $existingDay")

                    updateDayTimeSlot(existingDay, task)
                    existingDay
                } else {
                    Log.d("schedule", "existing day is null")

                    val newTimeSlot = createTimeSlot(task)
                    val newDay = createDay(dayNumber, month, year, newTimeSlot)

                    newDay
                }
            }
        }

        Log.d("schedule", "day getting passed back is $createdDay")
        return createdDay
    }


    private suspend fun createTimeSlot(task: Task): TimeSlot = withContext(Dispatchers.IO) {
        Log.d("schedule", "creating time slot")

        var existingTimeSlot = runBlocking {
            scheduleRepository.getTimeSlotForTime(task.time)
        }

        if (existingTimeSlot != null) {
            Log.d("schedule", "time slot exists")

            val updatedTimeSlot = existingTimeSlot.copy(
                tasks = existingTimeSlot.tasks + task
            )
            runBlocking {
                scheduleRepository.updateTimeSlot(updatedTimeSlot).also {
                    Log.d("schedule", "timeslot is updated to this: $updatedTimeSlot")
                }
            }

            Log.d("schedule", "updated time slot with new task: $updatedTimeSlot")

            return@withContext updatedTimeSlot
        } else {
            val newTimeSlot = TimeSlot(time = task.time, tasks = listOf(task))
            var newTimeSlotId : Int = 0
            runBlocking {
                scheduleRepository.insertTimeSlot(newTimeSlot).also {
                    Log.d("schedule", "New TimeSlot inserted: $newTimeSlot")
                }

                newTimeSlotId = scheduleRepository.getTimeSlotId(newTimeSlot.time)
            }

            val createdTimeSlot = newTimeSlot.copy(timeSlotId = newTimeSlotId)

            Log.d("schedule", "created time slot is $createdTimeSlot")

            return@withContext createdTimeSlot
        }
    }

    private suspend fun createDay(dayNumber: Int, month: Int, year: Int, timeSlot: TimeSlot): Day = withContext(Dispatchers.IO) {
        Log.d("schedule", "creating new day")
        var newDayId: Int = 0
        val newDay = Day(
            dayName = getDayName(dayNumber, month, year),
            dayNumber = dayNumber,
            month = month,
            year = year,
            timeSlots = listOf(timeSlot)
        )

        runBlocking {
            scheduleRepository.insertDay(newDay).also {
                Log.d("schedule", "new day inserted: $newDay")
            }

            newDayId = scheduleRepository.getDayId(dayNumber, month, year)
        }

        val createdDay = newDay.copy(dayId = newDayId)

        Log.d("schedule", "creating new Day: $createdDay")

        return@withContext createdDay
    }

    private suspend fun updateDayTimeSlot(day: Day, task: Task) = withContext(Dispatchers.IO) {
        Log.d("schedule", "updating time slot for task: $task in day: $day")

        runBlocking {
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
    }

    private suspend fun insertDayTask(day: Day, task: Task) {
        Log.d("schedule", "tried to insert day: $day and task: $task")

        val dayTask = DayTask(dayId = day.dayId, taskId = task.taskId)
        withContext(Dispatchers.IO) {
            runBlocking {
                scheduleRepository.insertDayTask(dayTask)
            }
        }

        Log.d("schedule", "timeslot udpated: $dayTask")
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

        calendar.time = Date()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val startDate = calendar.time
        currentWeekStartDate = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault()).format(startDate)

        val weekDates = mutableListOf<Triple<Int, Int, Int>>()
        val week = mutableListOf<Day>()

        for (index in 0 until 7) {
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            weekDates.add(Triple(dayNumber, month, year))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        runBlocking {
            val existingDays = scheduleRepository.getDaysByDates(weekDates)

            calendar.time = Date()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            for ((dayNumber, month, year) in weekDates) {
                val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
                val day = existingDays.find { it.dayNumber == dayNumber && it.month == month && it.year == year }
                    ?: Day(
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
        }
        _allDays.postValue(week)
    }



    fun updateTimeSlotsForSelectedDay(day: Day) = viewModelScope.launch(Dispatchers.IO){
        selectedDay = day.dayNumber.toString() + ":" + day.month.toString() + ":" + day.year.toString()
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
        viewModelScope.launch {
            val previousWeekStartDate = getPreviousWeekDate(currentWeekStartDate)

            val previousWeek = withContext(Dispatchers.IO) {
                getWeekForDate(previousWeekStartDate)
            }

            if (previousWeek.isNotEmpty()) {
                _allDays.value = previousWeek
                currentWeekStartDate = previousWeekStartDate
                Log.d("WeekNavigation", "Updated currentWeekStartDate to: $currentWeekStartDate")
            } else {
                Log.e("WeekNavigation", "Failed to load previous week.")
            }
        }

    }

    private fun isValidDate(date: String): Boolean {
        return try {
            SimpleDateFormat("dd:MM:yyyy", Locale.getDefault()).parse(date) != null
        } catch (e: ParseException) {
            false
        }
    }

    fun getAllDays() = viewModelScope.launch(Dispatchers.IO){
        scheduleRepository.getAllDaysFromDatabase().observeForever { days ->
            _allDays.value = days
        }
    }

    fun loadNextWeekTasks() {
        viewModelScope.launch {
            val nextWeekStartDate = getNextWeekDate(currentWeekStartDate)

            val nextWeek = withContext(Dispatchers.IO) {
                getWeekForDate(nextWeekStartDate)
            }

            if (nextWeek.isNotEmpty()) {
                _allDays.value = nextWeek
                currentWeekStartDate = nextWeekStartDate
                Log.d("WeekNavigation", "updated currentWeekStartDate to: $currentWeekStartDate")
            } else {
                Log.e("WeekNavigation", "Failed to load next week.")
            }
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
        Log.d("schedule", "getting the previous week for date: $startDate")
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

    suspend fun getWeekForDate(startDate: String): List<Day> {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault())
        Log.d("schedule", "date passed is $startDate")
        calendar.time = sdf.parse(startDate) ?: return emptyList()

        // Calculate the start of the week (Monday)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = (currentDayOfWeek - Calendar.MONDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -diff)

        val mondayDate = calendar.time
        currentWeekStartDate = SimpleDateFormat("dd:MM:yyyy", Locale.getDefault()).format(mondayDate)

        val weekDates = mutableListOf<Triple<Int, Int, Int>>()
        val week = mutableListOf<Day>()

        for (i in 0 until 7) {
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            weekDates.add(Triple(dayNumber, month, year))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        var existingDays : List<Day> = emptyList()

        withContext(Dispatchers.IO){
            runBlocking {
                existingDays = scheduleRepository.getDaysByDates(weekDates)
            }
        }

        Log.d("schedule", "existing dates for this week are: $existingDays")

        calendar.time = sdf.parse(startDate) ?: return emptyList()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for ((dayNumber, month, year) in weekDates) {
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)

            val existingDay = existingDays.find { it.dayNumber == dayNumber && it.month == month && it.year == year }

            val day = existingDay ?: Day(
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

    fun updateHabitOrder(newOrder: List<Habit>) = viewModelScope.launch {
        habitRepository.updateHabitOrder(newOrder)
    }

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