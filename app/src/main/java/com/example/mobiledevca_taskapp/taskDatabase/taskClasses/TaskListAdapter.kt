package com.example.mobiledevca_taskapp.taskDatabase.taskClasses

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.fragments.UpdateDataDialogFragment
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskListAdapter.TaskViewHolder

class TaskListAdapter(fragmentManager: FragmentManager, taskAppViewModel: TaskViewModel): ListAdapter<Task, TaskViewHolder>(TASK_COMPARATOR) {
    private val fragmentManager = fragmentManager
    var taskAppViewModel : TaskViewModel = taskAppViewModel


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder.create(parent, fragmentManager, taskAppViewModel)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class TaskViewHolder(itemView: View, fragmentManager: FragmentManager, taskAppViewModel: TaskViewModel) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val taskNameView: TextView = itemView.findViewById(R.id.Task_Name)
        private val taskCheckView: CheckBox = itemView.findViewById(R.id.Task_Checkbox)
        private lateinit var taskDescriptionView: String
        private var taskId = 0
        private val fragmentManager = fragmentManager
        private val taskAppViewModel = taskAppViewModel

        // Dao stuff for the checkbox
        init
        {
            itemView.setOnClickListener(this)
        }

        fun bind(task: Task) {
            taskNameView.text = task.taskName
            taskDescriptionView = task.description
            taskId  = task.taskId

            taskNameView.contentDescription = "Task name: ${task.taskName}"
            //taskDescriptionView.contentDescription = "Task description: ${task.description ?: ""}"

            taskCheckView.isChecked = task.isChecked
            // Checkbox listener
            taskCheckView.setOnClickListener {
                ChangeCheckbox(task)
            }
        }

        companion object {
            fun create(parent: ViewGroup, fragmentManager: FragmentManager, taskAppViewModel: TaskViewModel): TaskViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item, parent, false)
                return TaskViewHolder(view, fragmentManager, taskAppViewModel)
            }
        }

        override fun onClick(v: View?) {

            // The Parameters of tasks are passed as Strings then converted to whatever
            val stringArray: ArrayList<String> = ArrayList()
            stringArray.add("4")    // This is how we know we came from TasksActivity
            stringArray.add(taskId.toString())
            stringArray.add(taskNameView.text.toString())
            stringArray.add(taskDescriptionView)



            val UpdateDataDialog = UpdateDataDialogFragment.newInstance(stringArray)
            UpdateDataDialog.show(fragmentManager, UpdateDataDialogFragment.TAG)
        }

        fun ChangeCheckbox(task: Task)
        {
            // Change Value in Database to correct version
            if (taskCheckView.isChecked)
            {
                task.isChecked = true
            }
            else
            {
                task.isChecked = false
            }
            taskAppViewModel.ChangeCheckbox(task)
        }

    }

    companion object {
        private val TASK_COMPARATOR = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.taskId == newItem.taskId
            }
        }
    }
}