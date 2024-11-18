package com.example.mobiledevca_taskapp.taskDatabase.habitClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter.HabitViewHolder

class HabitListAdapter : ListAdapter<Habit, HabitViewHolder>(HABIT_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        return HabitViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val habitNameView: TextView = itemView.findViewById(R.id.habitName)

        fun bind(habit: Habit) {
            habitNameView.text = habit.name
        }

        companion object {
            fun create(parent: ViewGroup): HabitViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.habit_recycler_item, parent, false)
                return HabitViewHolder(view)
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