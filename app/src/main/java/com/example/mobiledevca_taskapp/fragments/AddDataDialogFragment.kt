package com.example.mobiledevca_taskapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
private const val DIALOG_NAME = "param2"

class AddDataDialogFragment : DialogFragment(), AdapterView.OnItemSelectedListener {
    private var dialogType: String? = null
    private var dialogName: String? = null
    private lateinit var dialogView : View //So other functions may access the root view
    private lateinit var taskAppViewModel : TaskViewModel
    private lateinit var habitSpinnerItem : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogType = it.getString(DIALOG_TYPE)
            dialogName = it.getString(DIALOG_NAME)
        }

        val app = requireActivity().application as TaskAppApplication
        val factory = TaskViewModelFactory(app)
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    //Create the dialog buttons and elements
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = layoutInflater.inflate(R.layout.add_data_dialog_layout, null)

        //Task variables
        val taskName = dialogView.findViewById<EditText>(R.id.taskNameInput)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescriptionInput)

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
        habitSpinner.setSelection(0) //Set default selection to placeholder
        habitSpinner.onItemSelectedListener = this

        changeVisibility(dialogView) //Set appropriate layouts to visible

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Enter $dialogName Details")
            .setPositiveButton("Confirm") { dialog, _ ->
                //If tasks called it
                if (dialogType == "1") {
                    addTask(taskName.text.toString(), taskDescription.text.toString())
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
        view.findViewById<View>(R.id.stepCounterLayout).visibility = View.GONE
        view.findViewById<View?>(R.id.habitCheckboxLayout).visibility = View.VISIBLE

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
        habitSpinnerItem = parent?.getItemAtPosition(position).toString()

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
    fun addTask(taskName : String, taskDescription : String) {
        val task = Task(0, taskName, taskDescription)
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