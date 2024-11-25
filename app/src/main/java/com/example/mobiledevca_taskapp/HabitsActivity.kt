// Habits Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.broadcast_receivers.HabitResetReceiver
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.fragments.AddDataDialogFragment
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter

class HabitsActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager
    private lateinit var id : String
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_habit, getString(R.string.menu_habits))

        _recyclerview = findViewById(R.id.habitRecyclerView)
        val adapter = HabitListAdapter(taskViewModel)
        _recyclerview.adapter = adapter
        _recyclerview.layoutManager = LinearLayoutManager(this)

        taskViewModel.allHabits.observe(this as LifecycleOwner) { habits ->
            Log.d("debug", "Observed habits: ${habits?.size} items")
            habits?.let{ adapter.submitList(it)}
        }

        fragmentManager = supportFragmentManager

        id = getString(R.string.habits_id)
        name = getString(R.string.habits_name)

        val addHabitBtn: Button = findViewById(R.id.addHabitBtn)
        addHabitBtn.setOnClickListener{
            val addDataDialog = AddDataDialogFragment.newInstance(id, name)
            addDataDialog.show(
                fragmentManager, AddDataDialogFragment.TAG
            )
        }

        val resetHabitCountBtn: Button = findViewById(R.id.resetDailyBtn)
        resetHabitCountBtn.setOnClickListener{
            Log.d("debug", "Tried to reset")
            val intent = Intent(this, HabitResetReceiver::class.java)
            intent.putExtra("RESET_TYPE", 1)
            this.sendBroadcast(intent)
        }

        val deleteHabitsBtn: Button = findViewById(R.id.deleteHabitBtn)
        deleteHabitsBtn.setOnClickListener{
            taskViewModel.deleteAllHabits()
            Toast.makeText(this, "All habits deleted", Toast.LENGTH_SHORT).show()
        }
    }
}