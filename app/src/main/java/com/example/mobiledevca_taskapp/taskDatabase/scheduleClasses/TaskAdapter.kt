package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task

class TaskAdapter(
    private val tasks: List<Task>
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTaskName: TextView = view.findViewById(R.id.textViewTaskName)
        val textViewTaskDescription: TextView = view.findViewById(R.id.textViewTaskDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.textViewTaskName.text = task.taskName
        holder.textViewTaskDescription.text = task.description
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}

