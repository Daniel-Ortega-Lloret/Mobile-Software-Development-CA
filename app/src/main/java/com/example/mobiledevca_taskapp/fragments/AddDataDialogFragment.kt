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
import java.util.Locale

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val DIALOG_TYPE = "param1"
private const val DIALOG_NAME = "param2"

class AddDataDialogFragment : DialogFragment(), AdapterView.OnItemSelectedListener {
    private var dialogType: String? = null
    private var dialogName: String? = null
    private lateinit var taskAppViewModel : TaskViewModel
    private lateinit var habitSpinner : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogType = it.getString(DIALOG_TYPE)
            dialogName = it.getString(DIALOG_NAME)
        }
        Log.d("debug","Passed this argument: $dialogType")

        val app = requireActivity().application as TaskAppApplication
        val factory = TaskViewModelFactory(app)
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    //Create the dialog buttons and elements
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.add_data_dialog_layout, null)

        //Task View references
        val taskName = dialogView.findViewById<EditText>(R.id.taskNameInput)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescriptionInput)
        val taskTime = dialogView.findViewById<Button>(R.id.Task_Time)
        val taskDate = dialogView.findViewById<Button>(R.id.Task_Date)

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
            "1" -> "Enter Task Details"
            "2" -> "Enter Schedule Details"
            "3" -> "Enter Habits Details"
            else -> "Invalid DialogType"
        }


            // Time Dialog Logic
            val timePickerDialogListener: TimePickerDialog.OnTimeSetListener = object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    hour = hourOfDay
                    min = minute
                }
            }

            taskTime.setOnClickListener{
               val timePicker: TimePickerDialog = TimePickerDialog(
                   requireContext(), timePickerDialogListener, 12, 0, true)
               timePicker.show()
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
                val datePicker: DatePickerDialog = DatePickerDialog(
                    requireContext(), datePickerDialogListener, 2024, 0, 1)
                datePicker.show()
            }


        // Confirm button must change to save if it is an edit dialog
        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(Dialog_Title)
            .setPositiveButton("Confirm") { dialog, _ ->
                //If tasks called it
                if (dialogType == "1") {

                    val timeString = "%02d:%02d".format(hour, min)

                    //val day = taskDate.dayOfMonth
                    //val month = taskDate.month
                    //val year = taskDate.year
                    val dateString = "%02d:%02d:%04d".format(d, m, y)

                    var task: Task = Task(0, taskName.text.toString(), taskDescription.text.toString(), false, timeString, dateString)
                    addTask(task)
                    dialog.dismiss()
                }
                else if (dialogType == "2") {
                    //do something for schedule logic
                    dialog.dismiss()
                }
                else if (dialogType == "3") {
                    addHabit(habitName.text.toString())
                    dialog.dismiss()
                }

                else {
                    dialog.dismiss()
                }
            }

            .setNegativeButton("Cancel") { _, _ -> } //Do nothing for now
            .create()

    }
    //Sets appropriate layouts to visible depending on what activity instantiated the fragment
    private fun changeVisibility (view:View) {
        //Reset to invisible for edge-cases
        view.findViewById<View>(R.id.tasksSection).visibility = View.GONE
        view.findViewById<View>(R.id.habitSection).visibility = View.GONE

        when(dialogType) {
            //Task visibility
            getString(R.string.tasks_id) -> {
                view.findViewById<View>(R.id.tasksSection).visibility = View.VISIBLE
            }
            //Schedule visibility
            getString(R.string.schedule_id) -> {
                //set schedule layout elements to visible here
            }
            //Habit visibility
            getString(R.string.habits_id) -> {
                view.findViewById<View>(R.id.habitSection).visibility = View.VISIBLE
                view.findViewById<View>(R.id.habitCheckboxLayout).visibility = View.VISIBLE
            }
        }
    }
    //Spinner function for selection
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    //Do nothing if user selects nothing on spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    //Task database function
    fun addTask(task: Task) {
        taskAppViewModel.insertTask(task)
    }

    //Habit database function
    fun addHabit(habitName: String) {
        val habit = Habit(0, habitName)
        taskAppViewModel.insertHabit(habit)
    }

    //Factory object for creating instances of the fragment
    companion object {
        const val TAG = "AddDataDialog"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddDataDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TYPE, param1)
                    putString(DIALOG_NAME,param2)
                }
            }
    }

}