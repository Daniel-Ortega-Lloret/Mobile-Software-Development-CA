//Calendar Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot
import com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses.ScheduleAdapter
import com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses.TimeSlotAdapter

class ScheduleActivity : BaseActivity() {
    private lateinit var nextWeekBtn: Button
    private lateinit var previousWeekBtn: Button
    private var selectedDay: Day? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_schedule, getString(R.string.menu_schedule))

        previousWeekBtn = findViewById<Button>(R.id.previousWeekBtn)
        nextWeekBtn = findViewById<Button>(R.id.nextWeekBtn)

        val recyclerViewTimeSlots = findViewById<RecyclerView>(R.id.recyclerViewTimeSlots)
        recyclerViewTimeSlots.layoutManager = LinearLayoutManager(this)
        val timeSlotAdapter = TimeSlotAdapter(emptyList())
        recyclerViewTimeSlots.adapter = timeSlotAdapter

        taskViewModel.allDays.observe(this as LifecycleOwner) { weekTasks ->
            addDaysToLinearLayout(weekTasks)

            if (weekTasks.isNotEmpty()) {
                val monday = weekTasks.firstOrNull { it.dayName == "Mon" } ?: weekTasks[0]
                selectedDay = monday
                taskViewModel.updateTasksForSelectedDay(monday)
                updateDayBackgrounds(weekTasks)
            }

            updateDaysTextForWeek(weekTasks)
            updateDayBackgrounds(weekTasks)
        }

        taskViewModel.preLoadWeekTasks()

        taskViewModel.selectedDayTasks.observe(this as LifecycleOwner) { tasks ->
            val timeSlotsForThisDay = convertTasksToTimeSlots(tasks)

            timeSlotAdapter.submitList(timeSlotsForThisDay)
        }

        taskViewModel.selectedTimeSlots.observe(this as LifecycleOwner) { timeSlots ->
            timeSlotAdapter.submitList(timeSlots)
        }

        previousWeekBtn.setOnClickListener {
            Log.d("schedule", "clicked previous week")
            taskViewModel.loadPreviousWeekTasks()
        }

        nextWeekBtn.setOnClickListener {
            Log.d("schedule", "clicked next week")
            taskViewModel.loadNextWeekTasks()
        }

//        taskViewModel.populateTestData()

    }

    private fun addDaysToLinearLayout(weekTasks: List<Day>) {
        val linearLayout = findViewById<LinearLayout>(R.id.dayLayout)
        linearLayout.removeAllViews()

        weekTasks.forEach { day ->
            val dayItem = layoutInflater.inflate(R.layout.day_item, linearLayout, false) as LinearLayout

            val dayItemLabel: LinearLayout = dayItem.findViewById(R.id.dayItemLayout)
            val dayNameTextView = dayItem.findViewById<TextView>(R.id.textViewDayDate)
            val dayNumberTextView = dayItem.findViewById<TextView>(R.id.textViewDayNumber)

            dayNameTextView.text = day.dayName
            dayNumberTextView.text = day.dayNumber.toString()

            if (day == selectedDay) {
                dayItemLabel.setBackgroundResource(R.drawable.selected_day_background)
            } else {
                dayItemLabel.setBackgroundResource(R.drawable.day_item_background)
            }

            dayItem.setOnClickListener {
                if (selectedDay != day) {
                    selectedDay = day
                    taskViewModel.updateTasksForSelectedDay(day)
                    taskViewModel.updateTimeSlotsForSelectedDay(day)
                    updateDayBackgrounds(weekTasks)
                }
            }

            linearLayout.addView(dayItem)
        }
    }

    private fun updateDaysTextForWeek(weekTasks: List<Day>) {
        val linearLayout = findViewById<LinearLayout>(R.id.dayLayout)

        for (i in 0 until linearLayout.childCount) {
            val dayItem = linearLayout.getChildAt(i) as LinearLayout
            val dayNameTextView = dayItem.findViewById<TextView>(R.id.textViewDayDate)
            val dayNumberTextView = dayItem.findViewById<TextView>(R.id.textViewDayNumber)

            val day = weekTasks[i]

            dayNameTextView.text = day.dayName
            dayNumberTextView.text = day.dayNumber.toString()
        }
    }

    private fun updateDayBackgrounds(weekTasks: List<Day>) {
        val linearLayout = findViewById<LinearLayout>(R.id.dayLayout)
        for (i in 0 until linearLayout.childCount) {
            val dayItem = linearLayout.getChildAt(i) as LinearLayout
            val day = weekTasks[i]
            if (day == selectedDay) {
                dayItem.setBackgroundResource(R.drawable.selected_day_background)
            } else {
                dayItem.setBackgroundResource(R.drawable.day_item_background)
            }
        }
    }

    private fun convertTasksToTimeSlots(tasks: List<Task>): List<TimeSlot> {
        val allTimeSlots = (0 until 24).map { hour ->
            val hourString = String.format("%02d:00", hour)

            val tasksForHour = tasks.filter { it.time.startsWith(hourString) }

            TimeSlot(
                timeSlotId = hour,
                time = hourString,
                tasks = tasksForHour
            )
        }
        return allTimeSlots
    }
}
