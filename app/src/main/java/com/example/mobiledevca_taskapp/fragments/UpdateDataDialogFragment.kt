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