package com.example.mobiledevca_taskapp.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val DIALOG_TYPE = "param1"
private const val Task_Name = "param2"
private const val Task_Description = "param3"
private const val Task_Id = "param4"
private const val Task_Time = "param5"
private const val Task_Date = "param6"

class UpdateDataDialogFragment : DialogFragment(), AdapterView.OnItemSelectedListener {
    private var dialogType: String? = null
    private var taskName: String? = null
    private var taskDescription: String? = null
    private var taskId: String? = ""
    private var taskTime: String? = null
    private var taskDate: String? = null
    private lateinit var task: Task
    private lateinit var taskAppViewModel : TaskViewModel
    private lateinit var habitSpinner : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogType = it.getString(DIALOG_TYPE)
            taskName = it.getString(Task_Name)
            taskDescription = it.getString(Task_Description)
            taskId = it.getString(Task_Id)
            taskTime = it.getString(Task_Time)
            taskDate = it.getString(Task_Date)
        }
        // Pass this task into funtions for readability
        task = Task(taskId.toString().toInt(), taskName.toString(), taskDescription.toString(), false, taskTime.toString(), taskDate.toString())
        Log.d("debug","Passed this argument: $dialogType")

        val app = requireActivity().application as TaskAppApplication
        val factory = TaskViewModelFactory(app)
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    //Create the dialog buttons and elements
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.add_data_dialog_layout, null)

        //Task EditText variables
        val taskName = dialogView.findViewById<EditText>(R.id.taskNameInput)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescriptionInput)

        // Calender and Time Variables
        val taskTime = dialogView.findViewById<Button>(R.id.Task_Time)
        val taskDate = dialogView.findViewById<Button>(R.id.Task_Date)

        // Set The Button Text To Be The Time And Date Previously Selected
        if (TimeNotNull(task.time))
        {
            taskTime.setText(task.time)
        }
        if (CalenderNotNull(task.date))
        {
            taskDate.setText(task.date)
        }


        // For Storing Time And Date
        var hour: Int? = null
        var min: Int? = null

        var d: Int? = null
        var m: Int? = null
        var y: Int? = null

        //Habit variables
        val habitName = dialogView.findViewById<EditText>(R.id.dialogHabitNameInput)
        val habitSpinner = dialogView.findViewById<Spinner>(R.id.habit_spinner)
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.habit_spinner_items,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        habitSpinner.adapter = spinnerAdapter
        habitSpinner.onItemSelectedListener = this

        changeVisibility(dialogView) //Set appropriate layouts to visible

        /*     The titles of the Dialog button need to change
                      based on which type it is */
        val Dialog_Title = when(dialogType)
        {
            "4" -> "Edit Task Details"
            "5" -> "Edit Schedule Details"
            "6" -> "Edit Habit Details"
            else -> "Invalid DialogType"
        }


        // Confirm button must change to save if it is an edit dialog
        val DialogConfirm = when(dialogType)
        {
            "4" -> "Save"
            "5" -> "Save"
            "6" -> "Save"
            else -> "Confirm"
        }


        // Time Dialog Logic
        val timePickerDialogListener: TimePickerDialog.OnTimeSetListener = object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                hour = hourOfDay
                min = minute
            }
        }

        taskTime.setOnClickListener{
            // We can pre set the time to the last one selected, only if it has been done before
            if (TimeNotNull(task.time))
            {
                val timePicker: TimePickerDialog = TimePickerDialog(    // Set Hours To Prev using / slicing of time string
                    requireContext(), timePickerDialogListener, task.time.substring(0,2).toInt(), task.time.substring(3,5).toInt(), true
                )
                timePicker.show()
            }
            else
            {
                val timePicker: TimePickerDialog = TimePickerDialog(    // Set Time To A Default
                    requireContext(), timePickerDialogListener, 12, 0, true
                )
                timePicker.show()
            }

        }
        // End Of Time Dialog Logic

        // Start of Date Dialog
        val datePickerDialogListener: DatePickerDialog.OnDateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                d = dayOfMonth
                m = month
                y = year
            }
        }

        taskDate.setOnClickListener{
            if (CalenderNotNull(task.date))
            {
                val datePicker: DatePickerDialog = DatePickerDialog(
                    requireContext(), datePickerDialogListener, task.date.substring(6, 10).toInt(), task.date.substring(3,5).toInt(), task.date.substring(0,2).toInt())
                datePicker.show()
            }
            else
            {
                val datePicker: DatePickerDialog = DatePickerDialog(
                    requireContext(), datePickerDialogListener, 2024, 0, 1)
                datePicker.show()
            }

        }


        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(Dialog_Title)
            .setNeutralButton("Delete") {_, _ ->} // Do nothing until clicked
            .setPositiveButton(DialogConfirm) { dialog, _ ->

            }
            .setNegativeButton("Cancel") { _, _ -> } //Do nothing for now
            .create().also { dialog ->
                dialog.setOnShowListener{
                    // Confirm Button Pressed
                    val confirmBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    confirmBtn.setOnClickListener{
                        // Task Must Take The Edit Text Values To Save Changed
                        task = Task(task.taskId, taskName.text.toString(), taskDescription.text.toString(), false, taskTime.toString(), taskDate.toString())
                        if (task != null) {
                            updateTask(task)
                        }
                        dialog.dismiss()
                    }

                    val deleteBtn = dialog.getButton((AlertDialog.BUTTON_NEUTRAL))
                    deleteBtn.setOnClickListener {
                        // We just use the initialised task at the top of this class otherwise if we want to delete
                        if (task != null) {
                            deleteTask(task)
                        }
                        dialog.dismiss()
                    }
                }
            }
    }

    private fun TimeNotNull(t: String): Boolean {
        if (t != "null:null")
            return true
        return false
    }

    private fun CalenderNotNull(d: String): Boolean {
        if (d != "null:null:null")
            return true
        return false
    }

    //Sets appropriate layouts to visible depending on what activity instantiated the fragment
    private fun changeVisibility (view:View) {
        //Reset to invisible for edge-cases
        view.findViewById<View>(R.id.tasksSection).visibility = View.GONE
        view.findViewById<View>(R.id.habitSection).visibility = View.GONE
        view.findViewById<View>(R.id.tasksSection).visibility = View.GONE

        when(dialogType) {
            // Update Task visibility
            "4" -> {
                view.findViewById<View>(R.id.tasksSection).visibility = View.VISIBLE

                // Prefill the textbox with the task current text
                val Title_Textbox =  view.findViewById<EditText>(R.id.taskNameInput)
                val Description_Textbox =  view.findViewById<EditText>(R.id.taskDescriptionInput)
                Title_Textbox.setText(taskName)
                Description_Textbox.setText(taskDescription)

                // Send To Dao


            }
        }
    }
    //Spinner function for selection
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    //Do nothing if user selects nothing on spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    fun updateTask(task: Task)
    {
        taskAppViewModel.updateTask(task)
    }

    // We just need to pass the task id
    fun deleteTask(task: Task)
    {
        taskAppViewModel.deleteTask(task.taskId)
    }

    //Factory object for creating instances of the fragment
    companion object {
        const val TAG = "UpdateDataDialog"
        @JvmStatic
        fun newInstance(param1: ArrayList<String>) =
            UpdateDataDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TYPE, param1[0])
                    putString(Task_Id, param1[1])
                    putString(Task_Name, param1[2])
                    putString(Task_Description, param1[3])
                    putString(Task_Time, param1[4])
                    putString(Task_Date, param1[5])

                }
            }
    }

}