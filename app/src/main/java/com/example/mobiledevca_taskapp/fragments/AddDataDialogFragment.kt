package com.example.mobiledevca_taskapp.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
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
import java.util.Calendar
import com.google.android.material.textfield.TextInputEditText

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val DIALOG_TYPE = "param1"
private const val DIALOG_NAME = "param2"

class AddDataDialogFragment : DialogFragment(), AdapterView.OnItemSelectedListener {
    private var dialogType: String? = null
    private var dialogName: String? = null
    private lateinit var dialogView : View //So other functions may access the root view
    private lateinit var taskAppViewModel : TaskViewModel
    private var habitSpinnerItem : Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogType = it.getString(DIALOG_TYPE)
            dialogName = it.getString(DIALOG_NAME)
        }

        val app = requireActivity().application as TaskAppApplication
        val factory = TaskViewModelFactory(app, app.applicationScope)
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    //Create the dialog buttons and elements
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = layoutInflater.inflate(R.layout.add_data_dialog_layout, null)

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
        val habitName = dialogView.findViewById<TextInputEditText>(R.id.dialogHabitNameInput)
        val habitResetRadioGroup = dialogView.findViewById<RadioGroup>(R.id.habitTimeSection)
        val habitPositiveCheckbox = dialogView.findViewById<CheckBox>(R.id.habitPositiveCheckbox)
        val habitNegativeCheckbox = dialogView.findViewById<CheckBox>(R.id.habitNegativeCheckbox)
        val stepCounter = dialogView.findViewById<TextInputEditText>(R.id.stepCounterTextInput)
        val habitSpinner = dialogView.findViewById<Spinner>(R.id.habit_spinner)
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.habit_spinner_items,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        habitSpinner.adapter = spinnerAdapter
        habitSpinner.setSelection(0) //Set default selection to placeholder
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

                    // We must set dateString Here. And Last line changes button to display after we set date
                    taskTime.setText("%02d:%02d".format(hour, min))

                }
            }

            taskTime.setOnClickListener{
                // If We Set A time And Then Want To Change it Again Before Saving, We Should Be Choosing From Where We Left Off
                if (taskTime.text != "Set Time")
                {
                    val timePicker: TimePickerDialog = TimePickerDialog(    // Set Hours To Prev using / slicing of time string
                        requireContext(), timePickerDialogListener, taskTime.text.substring(0,2).toInt(), taskTime.text.substring(3,5).toInt(), true
                    )
                    timePicker.show()
                }
                else {
                    val timePicker: TimePickerDialog = TimePickerDialog(
                        requireContext(), timePickerDialogListener, 12, 0, true)
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
                    taskDate.setText("%02d:%02d:%04d".format(d, m!! + 1, y)) // Months start at 0: January should be month 1
                }
            }

            taskDate.setOnClickListener{
                // If We Were To Edit Our Date Before Saving We Want To Pick From Our Last Selected Value
                if (taskDate.text != "Set Date")
                {
                    val datePicker: DatePickerDialog = DatePickerDialog(
                        requireContext(), datePickerDialogListener, taskDate.text.substring(6, 10).toInt(), taskDate.text.substring(3,5).toInt() - 1, taskDate.text.substring(0,2).toInt())
                    datePicker.show()
                }

                else
                {
                    // Used to open calendar on current month
                    val calendar = Calendar.getInstance()
                    val curYear = calendar.get(Calendar.YEAR)
                    val curMonth = calendar.get(Calendar.MONTH)
                    val curDate = calendar.get(Calendar.DATE)

                    val datePicker: DatePickerDialog = DatePickerDialog(
                        requireContext(), datePickerDialogListener, curYear, curMonth, curDate)
                    datePicker.show()
                }

            }


        // Confirm button must change to save if it is an edit dialog
        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(Dialog_Title)
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel") { _, _ -> } //Do nothing for now
            .create().also { dialog ->
                dialog.setOnShowListener {
                    val confirmBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    confirmBtn.setOnClickListener {
                        //Task Validation goes here


                        //Habit validation values
                        val habitNameText = habitName.text.toString().trim()
                        val habitPositiveState = habitPositiveCheckbox.isChecked
                        val habitNegativeState = habitNegativeCheckbox.isChecked
                        val stepCounterText = stepCounter.text.toString().trim()
                        val spinnerSelection = habitSpinner.selectedItemPosition
                        val habitResetValue: Int = when (habitResetRadioGroup.checkedRadioButtonId) {
                            R.id.habitDailyCounter -> 1
                            R.id.habitWeeklyCounter -> 2
                            R.id.habitMonthlyCounter -> 3
                            else -> 0
                        }
                        when (dialogType) {
                            "1" -> {
                                val timeString = "%02d:%02d".format(hour, min)
                                val dateString = "%02d:%02d:%04d".format(d, m, y)

                                var task: Task = Task(0, taskName.text.toString(), taskDescription.text.toString(), false, timeString, dateString)
                                addTask(task)
                                dialog.dismiss()
                            }
                            "2" -> {
                                //do something for schedule logic
                                dialog.dismiss()
                            }
                            "3" -> {
                                if (habitNameText.isEmpty()) {
                                    Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
                                }
                                else if (spinnerSelection == 0) {
                                    Toast.makeText(requireContext(), "Please select a Habit type", Toast.LENGTH_SHORT).show()
                                }
                                else if (spinnerSelection == 1 && !habitPositiveState && !habitNegativeState) {
                                    Toast.makeText(requireContext(), "Please check at least one counter", Toast.LENGTH_SHORT).show()
                                }
                                else if(spinnerSelection == 2 && stepCounterText.isEmpty()) {
                                    Toast.makeText(requireContext(),"Please enter amount of steps", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    //Initialized here to ensure validation check is passed before inserting to entity table
                                    var habitCountCheckValue: Int? = 0
                                    if (habitPositiveState && habitNegativeState) {
                                        habitCountCheckValue = 0
                                    }
                                    else if (habitPositiveState && !habitNegativeState) {
                                        habitCountCheckValue = 1
                                    }
                                    else if (!habitPositiveState && habitNegativeState) {
                                        habitCountCheckValue = -1
                                    }

                                    if (spinnerSelection == 1) {
                                        addHabit(habitName.text.toString(), habitResetValue, habitCountCheckValue, habitSpinnerItem, 0)
                                    }
                                    else if (spinnerSelection == 2) {
                                        addHabit(habitName.text.toString(), habitResetValue, habitCountCheckValue, habitSpinnerItem,
                                            stepCounter.text.toString().toInt()
                                        )
                                    }

                                    dialog.dismiss()
                                }
                            }
                            else -> dialog.dismiss()
                        }
                    }
                }
            }
    }
    //Sets appropriate layouts to visible depending on what activity instantiated the fragment
    private fun changeVisibility (view:View) {
        //Reset to invisible for edge-cases
        view.findViewById<View>(R.id.tasksSection).visibility = View.GONE
        view.findViewById<View>(R.id.habitSection).visibility = View.GONE
        view.findViewById<View>(R.id.stepCounterLayout).visibility = View.GONE
        view.findViewById<View?>(R.id.habitCheckboxLayout).visibility = View.GONE

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
            }
        }
    }
    //Spinner function for selection
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        habitSpinnerItem = position

        if (view != null) {
            if (position == 0){
                dialogView.findViewById<View>(R.id.stepCounterLayout).visibility = View.GONE
                dialogView.findViewById<View?>(R.id.habitCheckboxLayout).visibility = View.GONE
            }
            else if (position == 1) {
                dialogView.findViewById<View>(R.id.stepCounterLayout).visibility = View.GONE
                dialogView.findViewById<View?>(R.id.habitCheckboxLayout).visibility = View.VISIBLE
            }
            else if (position == 2) {
                dialogView.findViewById<View?>(R.id.habitCheckboxLayout).visibility = View.GONE
                dialogView.findViewById<View>(R.id.stepCounterLayout).visibility = View.VISIBLE
            }
        }
    }

    //Do nothing if user selects nothing on spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {
        //This is just here because the class using a spinner needs to implement it
    }

    //Task database function
    fun addTask(task: Task) {
        taskAppViewModel.insertTask(task)
    }

    //Habit database function
    private fun addHabit(habitName: String, habitResetValue: Int?, habitCountCheckValue: Int?, habitSwitchValue : Int?, stepTotalValue: Int?) {
        val habit = Habit(0, habitName, habitResetValue, habitCountCheckValue, habitSwitchValue,0, 0, stepTotalValue)
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