// Habits Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.Manifest
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.broadcast_receivers.HabitResetReceiver
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.fragments.AddDataDialogFragment
import com.example.mobiledevca_taskapp.services.StepCounterService
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter

class HabitsActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager
    private var stepCounterService: StepCounterService? = null
    private var isBound = false
    private lateinit var id : String
    private lateinit var name: String
    private val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            Log.d("debug", "onService is called")
            val binder = service as StepCounterService.StepCounterBinder
            stepCounterService = binder.getService()
            stepCounterService?.setTaskViewModel(taskViewModel)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            stepCounterService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_habit, getString(R.string.menu_habits))

        // Check and request permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION
                )
            } else {
                // If permission granted, start service
                startStepCounterService()
            }
        } else {
            // No permission needed for Android 9 and below
            startStepCounterService()
        }

        _recyclerview = findViewById(R.id.habitRecyclerView)
        val adapter = HabitListAdapter(taskViewModel)
        _recyclerview.adapter = adapter
        _recyclerview.layoutManager = LinearLayoutManager(this)

        taskViewModel.allHabits.observe(this as LifecycleOwner) { habits ->
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
//            Log.d("debug", "Tried to reset")
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

    private fun startStepCounterService() {
        Log.d("debug", "Starting service bind")
        val serviceIntent = Intent(this, StepCounterService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        //Android 8.0 requirements
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(serviceIntent)
        }
        else {
            startService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepCounterService()
        } else {
            Toast.makeText(this, "Permission denied. Cannot count steps.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    companion object {
        const val REQUEST_ACTIVITY_RECOGNITION = 1001
    }
}