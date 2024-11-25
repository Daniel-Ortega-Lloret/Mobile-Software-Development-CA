package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter.HabitViewHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitListAdapter(private val taskViewModel: TaskViewModel) : ListAdapter<Habit, HabitViewHolder>(HABIT_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        return HabitViewHolder.create(parent, taskViewModel)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, taskViewModel)
    }

    class HabitViewHolder(itemView: View, taskViewModel: TaskViewModel) : RecyclerView.ViewHolder(itemView) {
        private val habitNameView: TextView = itemView.findViewById(R.id.habitName)
        private var habitCountText: TextView = itemView.findViewById(R.id.habitCountText)
        private val habitAddBtn: FloatingActionButton = itemView.findViewById(R.id.habitCounterAddBtn)
        private val habitRemoveBtn: FloatingActionButton = itemView.findViewById(R.id.habitCounterRemoveBtn)
        private val resetCountText: TextView = itemView.findViewById(R.id.habitResetCounterText)

        fun bind(habit: Habit, taskViewModel: TaskViewModel) {
            habitNameView.text = habit.habitName
            habitCountText.text = formatCountText(habit.habitCount)

            val resetValue: Int? = habit.habitReset
            val stringResetText : String = when (resetValue) {
                1 -> {
                    "Resets Daily"
                }

                2 -> {
                    "Resets Weekly"
                }

                3 -> {
                    "Resets Monthly"
                }
                else -> ""
            }
            resetCountText.text = stringResetText

            //Accessibility for screen reader
            habitNameView.contentDescription = "Habit name for habit number ${habit.habitId.plus(1)} is ${habit.habitName}"
            resetCountText.contentDescription = "This habit is set to: $stringResetText"

            habitAddBtn.setOnClickListener(null)
            habitRemoveBtn.setOnClickListener(null)

            if (habit.habitCountCheck == 0) {
                addPositiveListener(habitAddBtn, habit, habitCountText, taskViewModel)
                addNegativeListener(habitRemoveBtn, habit, habitCountText, taskViewModel)
            }
            else if (habit.habitCountCheck == 1) {
                addPositiveListener(habitAddBtn, habit, habitCountText, taskViewModel)

            }
            else if (habit.habitCountCheck == -1) {
                addNegativeListener(habitRemoveBtn, habit, habitCountText, taskViewModel)
            }
        }

        private fun formatCountText(count: Int?): String {
            val countStr : String = if (count!! > 0) {
                "+$count"
            } else {
                count.toString()
            }
            return countStr
        }

        private fun addPositiveListener(habitAddBtn: FloatingActionButton,habit : Habit, habitCountText: TextView, taskViewModel: TaskViewModel) {
            habitAddBtn.setOnClickListener{
                val newCount : Int? = habit.habitCount?.plus(1)
                habitCountText.text = formatCountText(newCount)
                if (newCount != null) {
                    taskViewModel.updateHabitCount(habit.habitId, newCount)
                }
            }
        }

        private fun addNegativeListener(habitRemoveBtn: FloatingActionButton, habit: Habit, habitCountText: TextView, taskViewModel: TaskViewModel) {
            habitRemoveBtn.setOnClickListener{
                val newCount : Int? = habit.habitCount?.minus(1)
                habitCountText.text = formatCountText(newCount)
                if (newCount != null) {
                    taskViewModel.updateHabitCount(habit.habitId, newCount)
                }
            }
        }
        companion object {
            fun create(parent: ViewGroup, taskViewModel: TaskViewModel): HabitViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.habit_counter_recycler_item, parent, false)
                return HabitViewHolder(view, taskViewModel)
            }
        }
    }

    companion object {
        private val HABIT_COMPARATOR = object : DiffUtil.ItemCallback<Habit>() {
            override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
                return oldItem.habitId == newItem.habitId
            }

            override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
                return oldItem == newItem
            }
        }
    }
}