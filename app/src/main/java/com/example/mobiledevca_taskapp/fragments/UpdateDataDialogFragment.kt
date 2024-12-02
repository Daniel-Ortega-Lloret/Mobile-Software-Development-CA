package com.example.mobiledevca_taskapp.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class UpdateDataDialogFragment : DialogFragment() {
    //Generic variables
    private var dialogType: String? = null
    private lateinit var taskAppViewModel : TaskViewModel

    //Task variables
    private var taskId: String? = ""
    private var taskName: String? = null
    private var taskDescription: String? = null
    private var taskTime: String? = null
    private var taskDate: String? = null
    private lateinit var task: Task

    //Habit variables
    private var habitId: Int = 0
    private var habitName: String = ""
//    private lateinit var habitNameTextView: EditText
    private var habitReset: Int = 0
    private var habitCountCheck: Int = 1
    private var habitSwitch: Int = 0
    private var habitTotalStepCount: Int = 0
//    private lateinit var habitTotalStepsTextView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataMap = arguments?.keySet()?.associateWith { key->
            arguments?.getString(key).orEmpty()
        }
        Log.d("debug", "in the update fragment")
        dataMap?.let {
            //Generic assignment
            dialogType = it["DIALOG_TYPE"]

            //Task assignment
            taskId = (it["Task_Id"] ?: 0).toString()
            taskName = it["Task_Name"] ?: "Blank"
            taskDescription = it["Task_Description"] ?: "Blank"
            taskTime = it["Task_Time"] ?: "Blank"
            taskDate = it["Task_Date"] ?: "Blank"

            //Habit assignment
            habitId = it["Habit_Id"]?.toInt() ?: 0
            habitName = it["Habit_Name"] ?:"Blank"
            habitReset = it["Habit_Reset"]?.toInt() ?: 0
            habitCountCheck = it["Habit_CountCheck"]?.toInt() ?: 0
            habitSwitch = it["Habit_Switch"]?.toInt() ?: 0
            habitTotalStepCount = it["Habit_TotalSteps"]?.toInt() ?: 0
        }

        // Pass this task into funtions for readability
        task = Task(taskId.toString().toInt(), taskName.toString(), taskDescription.toString(), false, taskTime.toString(), taskDate.toString())
        Log.d("debug","Passed this argument: $dialogType")

        val app = requireActivity().application as TaskAppApplication
        val factory = TaskViewModelFactory(app, app.applicationScope)
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
            var dateSplit = task.date.split(":")
            var month = dateSplit[1].toInt()
            month = (month + 1)

            var String = "%02d:%02d:%02d".format(dateSplit[0].toInt(), month, dateSplit[2].toInt())
            taskDate.setText(String)
        }


        // For Storing Time And Date
        var hour: Int? = null
        var min: Int? = null
        var timeString = task.time

        var d: Int? = null
        var m: Int? = null
        var y: Int? = null
        var dateString = task.date

        //Habit variables
        val habitNameEditText = dialogView.findViewById<EditText>(R.id.dialogHabitNameInput)
        val habitResetRadioGroup = dialogView.findViewById<RadioGroup>(R.id.habitTimeSection)
        val habitPositiveCheckbox = dialogView.findViewById<CheckBox>(R.id.habitPositiveCheckbox)
        val habitNegativeCheckbox = dialogView.findViewById<CheckBox>(R.id.habitNegativeCheckbox)
        val stepCounter = dialogView.findViewById<TextInputEditText>(R.id.stepCounterTextInput)

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
            "4" -> "Save Task"
            "5" -> "Save Schedule"
            "6" -> "Save Habit"
            else -> "Confirm"
        }


        // Selecting and saving the time to timeString
        val timePickerDialogListener: TimePickerDialog.OnTimeSetListener = object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                hour = hourOfDay
                min = minute

                timeString = "%02d:%02d".format(hour, min)
                taskTime.setText(timeString)
            }
        }
        // Setting up for above
        taskTime.setOnClickListener{
            // If We Were To Edit Our Time Before Saving We Want To Pick From Our Last Selected Value
            if (taskTime.text != "Set Time")
            {
                val timePicker: TimePickerDialog = TimePickerDialog(    // Set Hours To Prev using / slicing of time string
                    requireContext(), timePickerDialogListener, taskTime.text.substring(0,2).toInt(), taskTime.text.substring(3,5).toInt(), true
                )
                timePicker.show()
            }
            // We can pre set the time to the last one selected, only if it has been done before
            else if (TimeNotNull(task.time))
            {
                val timePicker: TimePickerDialog = TimePickerDialog(    // Set Hours To Prev using / slicing of time string
                    requireContext(), timePickerDialogListener, task.time.substring(0,2).toInt(), task.time.substring(3,5).toInt(), true
                )
                timePicker.show()

            }
            else    // Set time to 12 if it hasnt been chosen yet
            {
                val timePicker: TimePickerDialog = TimePickerDialog(    // Set Time To A Default
                    requireContext(), timePickerDialogListener, 12, 0, true
                )
                timePicker.show()
            }

        }
        // End Of Time Dialog Logic

        // Selecting and saving the date to dateString
        val datePickerDialogListener: DatePickerDialog.OnDateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                d = dayOfMonth
                m = month
                y = year

                // We must set dateString Here. And Last line changes button to display after we set date
                dateString = "%02d:%02d:%04d".format(d, m, y)
                var displayString = "%02d:%02d:%04d".format(d, m!! + 1, y) // Months start at 0, But Jan should be month 1
                taskDate.setText(displayString)
            }
        }
        // Set the datepicker for above
        taskDate.setOnClickListener{
            // If We Were To Edit Our Date Before Saving We Want To Pick From Our Last Selected Value
            if (taskDate.text != "Set Date")
            {
                val datePicker: DatePickerDialog = DatePickerDialog(
                    requireContext(), datePickerDialogListener, taskDate.text.substring(6, 10).toInt(), taskDate.text.substring(3,5).toInt() - 1, taskDate.text.substring(0,2).toInt())
                datePicker.show()
            }
            // If We Are Editing A Date
            else if (CalenderNotNull(task.date)) // If Date Exists Already Then PreFill It
            {
                val datePicker: DatePickerDialog = DatePickerDialog(
                    requireContext(), datePickerDialogListener, task.date.substring(6, 10).toInt(), task.date.substring(3,5).toInt(), task.date.substring(0,2).toInt())
                datePicker.show()
            }
            else    // Else Have Todays Day Selected
            {
                val calendar = Calendar.getInstance()
                val curYear = calendar.get(Calendar.YEAR)
                val curMonth = calendar.get(Calendar.MONTH)
                val curDate = calendar.get(Calendar.DATE)

                val datePicker: DatePickerDialog = DatePickerDialog(
                    requireContext(), datePickerDialogListener, curYear, curMonth, curDate)
                datePicker.show()
            }

        } // End of date dialog


        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(Dialog_Title)
            .setNeutralButton("Delete") {_, _ ->} // Do nothing until clicked
            .setPositiveButton(DialogConfirm) { _, _ -> }
            .setNegativeButton("Cancel") { _, _ -> } //Do nothing for now
            .create().also { dialog ->
                dialog.setOnShowListener{
                    // Confirm Button Pressed
                    val confirmBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    confirmBtn.setOnClickListener{

                        //Habit Validation
                        val habitNameText = habitNameEditText.text.trim()
                        val habitPositiveState = habitPositiveCheckbox.isChecked
                        val habitNegativeState = habitNegativeCheckbox.isChecked
                        val stepCounterText = stepCounter.text.toString().trim()
                        val habitResetValue: Int = when (habitResetRadioGroup.checkedRadioButtonId) {
                            R.id.habitDailyCounter -> 1
                            R.id.habitWeeklyCounter -> 2
                            R.id.habitMonthlyCounter -> 3
                            else -> 0
                        }

                        // If We Came From TaskActivity
                        when (dialogType) {
                            "4" -> {
                                // Task Must Take The Edit Text Values To Save Changed
                                task = Task(task.taskId, taskName.text.toString(), taskDescription.text.toString(), false, timeString, dateString)

                                // Change values in database
                                updateTask(task)
                                dialog.dismiss()
                            }

                            // If We Came From HabitActivity
                            "6" -> {
                                if (habitNameText.isEmpty()) {
                                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                                } else if (habitSwitch == 1 && !habitPositiveState && !habitNegativeState) {
                                    Toast.makeText(requireContext(), "Please check at least one counter", Toast.LENGTH_SHORT).show()
                                } else if(habitSwitch == 2 && stepCounterText.isEmpty()) {
                                    Toast.makeText(requireContext(),"Please enter amount of steps", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (habitPositiveState && habitNegativeState) {
                                        habitCountCheck= 0
                                    }
                                    else if (habitPositiveState && !habitNegativeState) {
                                        habitCountCheck = 1
                                    }
                                    else if (!habitPositiveState && habitNegativeState) {
                                        habitCountCheck = -1
                                    }

                                    if (habitSwitch == 1) {
                                        val habit = Habit(habitId, habitNameEditText.text.toString(), habitResetValue, habitCountCheck)
                                        updateHabit(habit)
                                    } else if (habitSwitch == 2) {
                                        val habit = Habit(habitId, habitNameEditText.text.toString(), habitResetValue, 0, 0, 0, 0, stepCounter.text.toString().toInt())
                                        updateHabit(habit)
                                    }
                                    dialog.dismiss()
                                }

                            } else -> dialog.dismiss()
                        }
                    }

                    // Habit and Task Delets are seperated by the dialogType
                    val deleteBtn = dialog.getButton((AlertDialog.BUTTON_NEUTRAL))
                    deleteBtn.setOnClickListener {
                        when (dialogType) {
                            "4" -> {
                                // We use the initialised task at the top of the class
                                deleteTask(task)
                                dialog.dismiss()
                            }

                            "6" -> {
                                val habit = Habit(habitId, habitName)
                                deleteHabit(habit)
                                dialog.dismiss()
                            }
                        }

                    }//End Of deleteBtn
                }//End Of dialogListener
            }//End of .create
    }//End Of onCreateDialog

    // Checks if time wasnt set(null) or when inserting habits (time = Blank)
    private fun TimeNotNull(t: String): Boolean {
        if (t != "null:null" && t != "Blank")
            return true
        return false
    }

    // Checks if date wasnt set(null) or when inserting habits (date = Blank)
    private fun CalenderNotNull(d: String): Boolean {
        if (d != "null:null:null" && d != "Blank")
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
            }

            "6" -> {
                val habitNameEditText = view.findViewById<EditText>(R.id.dialogHabitNameInput)
                val habitResetRadioGroup = view.findViewById<RadioGroup>(R.id.habitTimeSection)
                val habitPositiveCheckbox = view.findViewById<CheckBox>(R.id.habitPositiveCheckbox)
                val habitNegativeCheckbox = view.findViewById<CheckBox>(R.id.habitNegativeCheckbox)
                val totalSteps = view.findViewById<TextInputEditText>(R.id.stepCounterTextInput)
                val habitResetMap = mapOf(
                    1 to R.id.habitDailyCounter,
                    2 to R.id.habitWeeklyCounter,
                    3 to R.id.habitMonthlyCounter
                )
                val radioButtonId = habitResetMap[habitReset] ?: -1

                view.findViewById<View>(R.id.habitSection).visibility = View.VISIBLE
                view.findViewById<View>(R.id.habit_spinner).visibility = View.GONE

                habitNameEditText.setText(habitName)
                if (radioButtonId != -1){
                    habitResetRadioGroup.check(radioButtonId)
                } else {
                    habitResetRadioGroup.clearCheck()
                }
                Log.d("debug", "mode is $habitSwitch")
                //Counter Item
                if (habitSwitch == 1) {
                    Log.d("debug", "im a count habit")
                    view.findViewById<View>(R.id.habitCheckboxLayout).visibility = View.VISIBLE
                    view.findViewById<View>(R.id.stepCounterLayout).visibility = View.GONE

                    //If both are checked
                    if (habitCountCheck == 0) {
                        habitPositiveCheckbox.isChecked = true
                        habitNegativeCheckbox.isChecked = true
                    }
                    //If positive is checked
                    else if (habitCountCheck == 1) {
                        habitPositiveCheckbox.isChecked = true
                        habitNegativeCheckbox.isChecked = false
                    }
                    //If negative is checked
                    else if (habitCountCheck == -1) {
                        habitPositiveCheckbox.isChecked = false
                        habitNegativeCheckbox.isChecked = true
                    }
                }
                //Step Counter Item
                else if (habitSwitch == 2) {
                    Log.d("debug", "im a count habit")
                    view.findViewById<View>(R.id.habitCheckboxLayout).visibility = View.GONE
                    view.findViewById<View>(R.id.stepCounterLayout).visibility = View.VISIBLE

                    val stringTotalStepCount = habitTotalStepCount.toString()
                    totalSteps.setText(stringTotalStepCount)
                }
            }
        }
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

    fun updateHabit(habit: Habit) {
        taskAppViewModel.updateHabit(habit)
    }

    fun deleteHabit(habit: Habit) {
        taskAppViewModel.deleteHabit(habit)
    }

    //Factory object for creating instances of the fragment
    companion object {
        const val TAG = "UpdateDataDialog"
        @JvmStatic
        fun newInstance(data: HashMap<String, String>) =
            UpdateDataDialogFragment().apply {
                arguments = Bundle().apply {
                    for ((key, value) in data) {
                        putString(key, value)
                    }

                }
            }
    }

}