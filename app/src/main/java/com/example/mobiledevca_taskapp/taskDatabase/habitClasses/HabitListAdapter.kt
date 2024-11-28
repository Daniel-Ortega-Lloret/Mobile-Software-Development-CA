package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.services.StepCounterService
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter.HabitCountViewHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitListAdapter(private val taskViewModel: TaskViewModel) : ListAdapter<Habit, RecyclerView.ViewHolder>(HABIT_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_COUNT -> HabitCountViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.habit_counter_recycler_item, parent, false),
                taskViewModel
            )
            TYPE_STEP -> StepCountViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.habit_step_counter_recycler_item, parent, false),
                taskViewModel
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val current = getItem(position)

        when(holder) {
            is HabitCountViewHolder -> holder.bind(current, taskViewModel)
            is StepCountViewHolder -> holder.bind(current, taskViewModel)
        }

    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).habitSwitch == 1){
            return 1
        }
        else if (getItem(position).habitSwitch == 2) {
            return 2
        }
        else {
            return -1
        }

    }

    class HabitCountViewHolder(itemView: View, taskViewModel: TaskViewModel) : RecyclerView.ViewHolder(itemView) {
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

            habitAddBtn.isEnabled = false
            habitAddBtn.setOnClickListener(null)
            habitRemoveBtn.isEnabled = false
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
            habitAddBtn.isEnabled = true
            habitAddBtn.setOnClickListener{
                val newCount : Int? = habit.habitCount?.plus(1)
                habitCountText.text = formatCountText(newCount)
                if (newCount != null) {
                    taskViewModel.updateHabitCount(habit.habitId, newCount)
                }
            }
        }

        private fun addNegativeListener(habitRemoveBtn: FloatingActionButton, habit: Habit, habitCountText: TextView, taskViewModel: TaskViewModel) {
            habitRemoveBtn.isEnabled = true
            habitRemoveBtn.setOnClickListener{
                val newCount : Int? = habit.habitCount?.minus(1)
                habitCountText.text = formatCountText(newCount)
                if (newCount != null) {
                    taskViewModel.updateHabitCount(habit.habitId, newCount)
                }
            }
        }
    }

    class StepCountViewHolder(itemView: View, taskViewModel: TaskViewModel) : RecyclerView.ViewHolder(itemView) {
        private val stepCountName: TextView = itemView.findViewById(R.id.stepCountName)
        private val stepResetText: TextView = itemView.findViewById(R.id.stepResetCounterText)
        private val stepCurrentNumber: TextView = itemView.findViewById(R.id.stepNumCurrent)
        private val stepTotalNumber: TextView = itemView.findViewById(R.id.stepNumTotal)

        fun bind(habit: Habit, taskViewModel: TaskViewModel) {
            taskViewModel.setStepItemAdded(true)
            stepCountName.text = habit.habitName
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
            stepResetText.text = stringResetText
            stepCurrentNumber.text =  habit.habitStepCount.toString()
            stepTotalNumber.text = habit.habitTotalStepCount.toString()

        }
    }

    companion object {

        private const val TYPE_COUNT = 1
        private const val TYPE_STEP = 2

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