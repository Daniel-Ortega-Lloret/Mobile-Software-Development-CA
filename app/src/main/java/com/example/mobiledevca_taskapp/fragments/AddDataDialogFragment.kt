package com.example.mobiledevca_taskapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
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

        //Task variables
        val taskName = dialogView.findViewById<EditText>(R.id.taskNameInput)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescriptionInput)

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

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Enter $dialogName Details")
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel") { _, _ -> } //Do nothing for now
            .create().also { dialog ->
                dialog.setOnShowListener {
                    val confirmBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

                    confirmBtn.setOnClickListener {
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
                                addTask(taskName.text.toString(), taskDescription.text.toString())
                                dialog.dismiss()
                            }
                            "2" -> {
                                //do something for schedule logic
                                dialog.dismiss()
                            }
                            "3" -> {
                                if (habitNameText.isEmpty()) {
                                    Toast.makeText(requireContext(), "Please enter a Habit habitName", Toast.LENGTH_SHORT).show()
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
                                    addHabit(habitName.text.toString(), habitResetValue, habitCountCheckValue, habitSpinnerItem)
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
    private fun addTask(taskName : String, taskDescription : String) {
        val task = Task(0, taskName, taskDescription)
        taskAppViewModel.insertTask(task)
    }

    //Habit database function
    private fun addHabit(habitName: String, habitResetValue: Int?, habitCountCheckValue: Int?, habitSwitchValue : Int?) {
        val habit = Habit(0, habitName, habitResetValue, habitCountCheckValue, habitSwitchValue)
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