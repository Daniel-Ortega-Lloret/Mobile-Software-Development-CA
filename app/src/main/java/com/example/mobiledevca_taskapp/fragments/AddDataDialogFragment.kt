package com.example.mobiledevca_taskapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
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

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val DIALOG_TYPE = "param1"

class AddDataDialogFragment : DialogFragment(), AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters
    private var dialog_type: String? = null
    private lateinit var taskAppViewModel : TaskViewModel
    private lateinit var habitSpinner : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialog_type = it.getString(DIALOG_TYPE)
        }

        val app = requireActivity().application as TaskAppApplication
        val factory = TaskViewModelFactory(app)
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.add_habit_layout, null)

        val habitName = dialogView.findViewById<EditText>(R.id.dialogHabitName)
        val habitSpinner = dialogView.findViewById<Spinner>(R.id.habit_spinner)

        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.habit_spinner_items,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        habitSpinner.adapter = spinnerAdapter

        habitSpinner.onItemSelectedListener = this

        return AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Enter Habit Details")
            .setPositiveButton("Confirm") { dialog, _ ->
                addHabit(habitName.text.toString())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { _, _ -> } //Do nothing for now
            .create()
    }

    fun addHabit(habitName: String) {
        val habit = Habit(0, habitName)
        taskAppViewModel.insertHabit(habit)
    }

    companion object {
        const val TAG = "AddDataDialog"
        @JvmStatic
        fun newInstance(param1: String) =
            AddDataDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TYPE, param1)
                }
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}