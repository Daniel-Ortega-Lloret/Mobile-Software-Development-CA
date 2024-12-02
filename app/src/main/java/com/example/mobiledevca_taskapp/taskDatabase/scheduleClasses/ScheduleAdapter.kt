package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day

class ScheduleAdapter(
    private val viewModel: TaskViewModel,
    private val context: Context,
    private val onDayClick: (Day) -> Unit
) : ListAdapter<Day, ScheduleAdapter.DayViewHolder>(DayDiffCallback()) {

    private var selectedDay: Day? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.day_item, parent, false)
        return DayViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = getItem(position)
        holder.bind(day, viewModel)
    }

    class DayViewHolder(view: View, private val adapter: ScheduleAdapter) : RecyclerView.ViewHolder(view) {
        private val textViewTimeLabel: LinearLayout = view.findViewById(R.id.textViewTimeLabel)
        private val textViewDayDate: TextView = view.findViewById(R.id.textViewDayDate)
        private val textViewDayNumber : TextView = view.findViewById(R.id.textViewDayNumber)

        fun bind(day: Day, taskViewModel: TaskViewModel) {
            textViewDayDate.text = day.dayName
            textViewDayNumber.text = "${day.dayNumber}"

            val isSelected = day == adapter.selectedDay
            textViewTimeLabel.background = ContextCompat.getDrawable(
                adapter.context,
                if (isSelected) R.drawable.selected_day_background else R.drawable.day_item_background
            )

            textViewTimeLabel.setOnClickListener {
                Log.d("schedule", "just clicked $day")
                taskViewModel.updateTasksForSelectedDay(day)
                adapter.selectedDay = day
                adapter.onDayClick(day)
            }
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








