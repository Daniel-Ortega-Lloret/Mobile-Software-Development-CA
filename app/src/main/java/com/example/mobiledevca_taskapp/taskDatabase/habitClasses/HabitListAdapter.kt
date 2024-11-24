package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

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
        private val habitDescriptionView: TextView = itemView.findViewById(R.id.habitDescription)
        private var habitCountText: TextView = itemView.findViewById(R.id.habitCountText)
        private val habitAddBtn: FloatingActionButton = itemView.findViewById(R.id.habitCounterAddBtn)
        private val habitRemoveBtn: FloatingActionButton = itemView.findViewById(R.id.habitCounterRemoveBtn)

        fun bind(habit: Habit, taskViewModel: TaskViewModel) {
            habitNameView.text = habit.habitName
            habitDescriptionView.text = habit.habitDescription
            habitCountText.text = habit.habitCount.toString()

            habitNameView.contentDescription = "Habit name for habit number ${habit.habitId.plus(1)} is ${habit.habitName}"
            habitDescriptionView.contentDescription = "Description for habit numer ${habit.habitId.plus(1)} is ${habit.habitDescription}"

            habitAddBtn.setOnClickListener{
                val newCount : Int? = habit.habitCount?.plus(1)
                habitCountText.text = habit.habitCount.toString()
                if (newCount != null) {
                    taskViewModel.updateHabitCount(habit.habitId, newCount)
                }
            }

            habitRemoveBtn.setOnClickListener{
                val newCount : Int? = habit.habitCount?.minus(1)
                habitCountText.text = habit.habitCount.toString()
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
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
                return oldItem.habitId == newItem.habitId
            }
        }
    }
}