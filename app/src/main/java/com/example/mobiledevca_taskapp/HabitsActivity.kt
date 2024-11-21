// Habits Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter

class HabitsActivity : BaseActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var _recyclerview: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_habit, getString(R.string.menu_habits))

        _recyclerview = findViewById(R.id.habitRecyclerView)
        val adapter = HabitListAdapter()
        _recyclerview.adapter = adapter
        _recyclerview.layoutManager = LinearLayoutManager(this)

        taskViewModel.allHabits.observe(this as LifecycleOwner) { habits ->
            habits?.let{ adapter.submitList(it)}
        }

        val addHabitBtn: Button = findViewById(R.id.addHabitBtn)
        addHabitBtn.setOnClickListener{
            inputDialog()
        }

        val deleteHabitsBtn: Button = findViewById(R.id.deleteHabitBtn)
        deleteHabitsBtn.setOnClickListener{
            taskViewModel.deleteAllHabits()
            Toast.makeText(this, "All habits deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun inputDialog()
    {
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)

        //Inflate Layout
        val inflate = layoutInflater
        val dialogLayout = inflate.inflate(R.layout.add_habit_layout, null)

        // Title For Card
        builder.setTitle("Enter Habit Details")
        // Habit Name
        val Card_Name = dialogLayout.findViewById<EditText>(R.id.dialogHabitName)

        builder.setView(dialogLayout)

        // Confirm Button To Accept Text Fields
        builder.setPositiveButton("Confirm", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                val habit = Habit(0, Card_Name.text.toString())
                taskViewModel.insertHabit(habit)
            }
        })

        // Cancel Button To Dismiss Text Fields
        builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                // Do Nothing for now
            }
        })

        // Build The Dialog And Show It
        var dialog: AlertDialog = builder.create()

        dialog.setOnShowListener{
//            val habitSpinner: Spinner = findViewById(R.id.habit_spinner)
//            habitSpinner.onItemSelectedListener = this
//            val spinnerAdapter = ArrayAdapter.createFromResource(
//                this, R.array.habit_spinner_items, android.R.layout.simple_spinner_item
//            )
//            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            habitSpinner.adapter = spinnerAdapter

        }

        dialog.show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Toast.makeText(this, "All habits deleted", Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}