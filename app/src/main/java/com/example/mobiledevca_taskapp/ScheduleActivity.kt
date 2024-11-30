//Calendar Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_schedule, getString(R.string.menu_schedule))


        // Initialize the RecyclerView for days
        val recyclerViewDays = findViewById<RecyclerView>(R.id.recyclerViewDays)
        recyclerViewDays.layoutManager = GridLayoutManager(this, 7)
        val dayAdapter = ScheduleAdapter(taskViewModel, this)
        recyclerViewDays.adapter = dayAdapter

        // Initialize the RecyclerView for time slots
        val recyclerViewTimeSlots = findViewById<RecyclerView>(R.id.recyclerViewTimeSlots)
        recyclerViewTimeSlots.layoutManager = LinearLayoutManager(this)
        val timeSlotAdapter = TimeSlotAdapter(emptyList())
        recyclerViewTimeSlots.adapter = timeSlotAdapter

        taskViewModel.allDays.observe(this as LifecycleOwner) { weekTasks ->
            Log.d("schedule", "Tasks for week: $weekTasks")
            dayAdapter.submitList(weekTasks)

            val timeSlots = generateTimeSlots(weekTasks)
            timeSlotAdapter.submitList(timeSlots)
        }

        taskViewModel.preLoadWeekTasks()
    }

    private fun generateTimeSlots(days: List<Day>): List<TimeSlot> {
        return (0 until 24).map { hour ->
            val hourString = String.format("%02d:00", hour)
            val tasksForHour = days.flatMap { day ->
                day.timeSlots.flatMap { timeSlot ->
                    timeSlot.tasks.filter { it.time.startsWith(hourString) }
                }
            }
            TimeSlot(
                timeSlotId = hour,
                time = hourString,
                tasks = tasksForHour
            )
        }
    }
}
