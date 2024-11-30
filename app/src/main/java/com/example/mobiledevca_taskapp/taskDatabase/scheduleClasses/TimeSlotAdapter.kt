package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.entities.Day
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot

class TimeSlotAdapter(private val timeSlots: List<TimeSlot>) : ListAdapter<TimeSlot, TimeSlotAdapter.TimeSlotViewHolder>(TimeSlotDiffCallback()) {

    class TimeSlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeLabel: TextView = view.findViewById(R.id.textViewTimeLabel)
        val taskRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewTasks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = getItem(position)

        holder.timeLabel.text = timeSlot.time

        holder.taskRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.taskRecyclerView.adapter = TaskAdapter(timeSlot.tasks)
    }

    class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
        override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem.timeSlotId == newItem.timeSlotId
        }

        override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem == newItem
        }
    }
}



