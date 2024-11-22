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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.fragments.AddDataDialogFragment
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter

class HabitsActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager

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

        fragmentManager = supportFragmentManager


        val addHabitBtn: Button = findViewById(R.id.addHabitBtn)
        addHabitBtn.setOnClickListener{
//            inputDialog()
            AddDataDialogFragment().show(
                fragmentManager, "AddDataDialog"
            )
        }

        val deleteHabitsBtn: Button = findViewById(R.id.deleteHabitBtn)
        deleteHabitsBtn.setOnClickListener{
            taskViewModel.deleteAllHabits()
            Toast.makeText(this, "All habits deleted", Toast.LENGTH_SHORT).show()
        }
    }
}