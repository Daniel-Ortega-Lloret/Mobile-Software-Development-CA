package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import android.content.Context
import android.util.Log
import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskListAdapter
class ScheduleAdapter(
    private val viewModel: TaskViewModel,
    private val context: Context
) : ListAdapter<Day, ScheduleAdapter.DayViewHolder>(DayDiffCallback()) {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewDayDate: TextView = view.findViewById(R.id.textViewDayDate)
        val textViewDayNumber : TextView = view.findViewById(R.id.textViewDayNumber)
        val timeSlotRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewTimeSlots)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.day_item, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = getItem(position)

        holder.textViewDayDate.text = day.dayName
        holder.textViewDayNumber.text = "${day.dayNumber}"

        holder.timeSlotRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

        if (day.timeSlots.isNotEmpty()) {
            val timeSlotAdapter = TimeSlotAdapter(day.timeSlots)
            holder.timeSlotRecyclerView.adapter = timeSlotAdapter
        }
    }

    class DayDiffCallback : DiffUtil.ItemCallback<Day>() {
        override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.dayId == newItem.dayId
        }

        override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem == newItem
        }
    }
}








