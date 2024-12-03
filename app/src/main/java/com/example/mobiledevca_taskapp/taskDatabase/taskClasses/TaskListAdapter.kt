package com.example.mobiledevca_taskapp.taskDatabase.taskClasses

import android.util.Log
import android.icu.util.Calendar
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

    // For Drag and Drop reorder
    fun moveItem(from: Int, to: Int)
    {
        // Get the current list
        val updateCurrentList = currentList.toMutableList()

        // Take the item you are moving and Remove it from List
        val item = updateCurrentList[from]
        updateCurrentList.removeAt(from)

        // Move it Back at The Right Index. List will automatically shift
        updateCurrentList.add(to, item)


        // Set The Position To Match The Stored Value
        updateCurrentList.forEachIndexed{index, task ->
            task.position = index
        }

        // Tell ViewModel to update the position in the database
        taskAppViewModel.updateOrder(updateCurrentList)

        // Updates the list. Runs on a seperate thread, so wont be correct straight after
        submitList(updateCurrentList)
        {
            Log.d("TaskListAdapter", "Current list updated to: $currentList")
        }



    }

    class TaskViewHolder(itemView: View, fragmentManager: FragmentManager, taskAppViewModel: TaskViewModel) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val taskNameView: TextView = itemView.findViewById(R.id.Task_Name)
        private val taskCheckView: CheckBox = itemView.findViewById(R.id.Task_Checkbox)
        private lateinit var taskDescriptionView: String
        private var taskId = 0
        private var taskTime = ""
        private var taskDate = ""
        private var taskTimeView: TextView = itemView.findViewById(R.id.RecyclerItemTime)
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
            // taskTime and taskDate is for passing to dialogs
            taskTime = task.time
            taskDate = task.date

            // For Screen Readers
            taskNameView.contentDescription = "Task name: ${task.taskName}"

            // Checkbox Listener
            taskCheckView.isChecked = task.isChecked
            taskCheckView.setOnClickListener {
                ChangeCheckbox(task)

            }


            // For Doing Calculations and displaying beside each recycler item
            taskTimeView.setText(ShowTime(task))
        }


        companion object {
            fun create(parent: ViewGroup, fragmentManager: FragmentManager, taskAppViewModel: TaskViewModel): TaskViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item, parent, false)
                return TaskViewHolder(view, fragmentManager, taskAppViewModel)
            }
        }

        override fun onClick(v: View?) {
            Log.d("debug", "task got clicked")
            // The Parameters of tasks are passed as Strings then converted to whatever
            val dataMap = hashMapOf<String, String>()
            dataMap["DIALOG_TYPE"] = "4"  // Identifies the source of data
            dataMap["Task_Id"] = taskId.toString()
            dataMap["Task_Name"] = taskNameView.text.toString()
            dataMap["Task_Description"] = taskDescriptionView
            dataMap["Task_Time"] = taskTime
            dataMap["Task_Date"] = taskDate
            val updateDataDialog = UpdateDataDialogFragment.newInstance(dataMap)
            updateDataDialog.show(fragmentManager, UpdateDataDialogFragment.TAG)
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

        // For calculating what to display for the time string in a recycler item
        private fun ShowTime(task: Task): String
        {
            var min = 0
            var hr = 0
            var d = 0
            var m = 0
            var y = 0
            var dateSplit: List<String>

            // If Neither Time Or Date Was Selected: Display nothing
            if ((task.time == "null:null") && (task.date == "null:null:null"))
            {
                return ""   // Just Display Nothing
            }


            // Next We Do The Calculations Based On Time vs Current Time
            val calendar = Calendar.getInstance()


            // Get The Current Date + Time
            val minute = calendar.get(Calendar.MINUTE)
            val hour = calendar.get(Calendar.HOUR_OF_DAY) // Hr of day = 24hr   / Hour =  12
            val date = calendar.get(Calendar.DATE)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            // Now Set A Calender to the Current Date + Time values
            val dateNow = Calendar.getInstance()
            dateNow.set(Calendar.MINUTE, minute)
            dateNow.set(Calendar.HOUR_OF_DAY, hour)
            dateNow.set(Calendar.DATE, date)
            dateNow.set(Calendar.MONTH, month)
            dateNow.set(Calendar.YEAR, year)

            // Check if its null to stop crash
            if (task.date != "null:null:null")
            {
                // Parsing the task date to do maths
                dateSplit = task.date.split(":")
                d = dateSplit[0].toInt()
                m = dateSplit[1].toInt()
                y = dateSplit[2].toInt()
            }
            // If We Dont Have A Date: Then It Becomes Todays Date
            else
            {
                d = calendar.get(Calendar.DATE)
                m = calendar.get(Calendar.MONTH)
                y = calendar.get(Calendar.YEAR)
            }

            // If Time Isnt Null Then Use It
            if (task.time != "null:null")
            {
                dateSplit = task.time.split(":")
                hr = dateSplit[0].toInt()
                min = dateSplit[1].toInt()
            }

            // Else Give 12am
            else
            {
                min = 0
                hr = 0
            }



            // Turn the task date into a calender
            val formatTaskDate = Calendar.getInstance()
            formatTaskDate.set(Calendar.MINUTE, min)
            formatTaskDate.set(Calendar.HOUR_OF_DAY, hr)
            formatTaskDate.set(Calendar.DATE, d)
            formatTaskDate.set(Calendar.MONTH, m)
            formatTaskDate.set(Calendar.YEAR, y)

            // This Finds The Time Difference Between The Task Date/Time And The Current Date/Time
            var Difference = formatTaskDate.timeInMillis - dateNow.timeInMillis

            // If The Task Date/Time is in the past
            if (Difference < 0)
            {
                return "Time Over"
            }

            // If Its More Than A Day: Dislay in Days
            else if ((Difference / 1000 / 60 / 60 / 24) >= 1)
            {
                Difference = Difference / 1000 / 60 / 60 / 24
                return ("%d Days Left").format(Difference)
            }

            // If Its Less Than A Day, But More or Equal To 1hr: Display in hours
            else if (((Difference / 1000/ 60 / 60 < 24) && (Difference / 1000 / 60 / 60 >= 1)))
            {
                Difference = Difference / 1000 / 60 / 60
                return ("%d Hours Left").format(Difference)
            }

            // Else Display in minutes
            else if ((Difference / 1000 / 60 < 60))
            {
                Difference = Difference / 1000 / 60
                return ("%d Mins Left").format(Difference)
            }

            return ""
        }

    }


    companion object {
        private val TASK_COMPARATOR = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.taskId == newItem.taskId
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem && oldItem.position == newItem.position
            }
        }
    }



}