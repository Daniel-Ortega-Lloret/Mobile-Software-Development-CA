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
import android.widget.TextView
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

class UpdateDataDialogFragment : DialogFragment(), AdapterView.OnItemSelectedListener {
    private var dialogType: String? = null
    private var taskName: String? = null
    private var taskDescription: String? = null
    private var taskId: String? = null
    private lateinit var taskAppViewModel : TaskViewModel
    private lateinit var habitSpinner : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogType = it.getString(DIALOG_TYPE)
            taskName = it.getString(Task_Name)
            taskDescription = it.getString(Task_Description)
            taskId = it.getString(Task_Id)
        }
        Log.d("debug","Passed this argument: $dialogType")

        val app = requireActivity().application as TaskAppApplication
        val factory = TaskViewModelFactory(app)
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    //Create the dialog buttons and elements
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.add_data_dialog_layout, null)

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
        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(Dialog_Title)
            .setPositiveButton(DialogConfirm) { dialog, _ ->
                //If tasks called it
                if (dialogType == "4")
                {
                    dialog.dismiss()
                }
                else {
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> } //Do nothing for now
            .create().also { dialog ->
                dialog.setOnShowListener{
                    val confirmBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    confirmBtn.setOnClickListener{
                        updateTask(taskId.toString().toInt(), taskName.text.toString(), taskDescription.text.toString())
                        dialog.dismiss()
                    }
                }
            }
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

    fun updateTask(taskId: Int, taskName: String, taskDescription: String)
    {
        val task = Task(taskId, taskName, taskDescription)
        taskAppViewModel.updateTask(task)
    }


    //Factory object for creating instances of the fragment
    companion object {
        const val TAG = "UpdateDataDialog"
        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String, param4: String) =
            UpdateDataDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TYPE, param1)
                    putString(Task_Name, param2)
                    putString(Task_Description, param3)
                    putString(Task_Id, param4)
                }
            }
    }

}