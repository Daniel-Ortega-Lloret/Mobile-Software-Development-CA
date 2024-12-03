// Habits Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.broadcast_receivers.HabitResetReceiver
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.fragments.AddDataDialogFragment
import com.example.mobiledevca_taskapp.services.StepCounterService
import com.example.mobiledevca_taskapp.taskDatabase.entities.Habit
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.HabitListAdapter
import com.example.mobiledevca_taskapp.taskDatabase.habitClasses.StepNotificationMaker

class HabitsActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager
    private var stepCounterService: StepCounterService? = null
    private var isBound = false
    private lateinit var id : String
    private lateinit var name: String
    private var connection: ServiceConnection? = null
    private lateinit var adapter:  HabitListAdapter
    private var pendingStepsProcessed = false

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_habit, getString(R.string.menu_habits))

        val permissions = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
        )
        // Check and request permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            // All permissions are granted, bind to the service
            bindServiceWithPermission()
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(this, permissions, REQUEST_ACTIVITY_RECOGNITION)
        }

        fragmentManager = supportFragmentManager

        _recyclerview = findViewById(R.id.habitRecyclerView)
        adapter = HabitListAdapter(fragmentManager, taskViewModel)
        _recyclerview.adapter = adapter
        _recyclerview.layoutManager = LinearLayoutManager(this)
        _recyclerview.itemAnimator = null

        val itemTouchHelper by lazy {
            val simpleItemTouchCallback =
                object :
                    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val adapter = recyclerView.adapter as HabitListAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition

                        adapter.moveItem(from, to)
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        // If Swiped
                    }
                }
            ItemTouchHelper(simpleItemTouchCallback)
        }
        itemTouchHelper.attachToRecyclerView(_recyclerview)

        taskViewModel.allHabits.observe(this as LifecycleOwner) { habits ->
            habits?.let { adapter.submitList(it) }
        }

        checkExistingItems()



        id = getString(R.string.habits_id)
        name = getString(R.string.habits_name)

        val addHabitBtn: Button = findViewById(R.id.addHabitBtn)
        addHabitBtn.setOnClickListener {
            val addDataDialog = AddDataDialogFragment.newInstance(id, name)
            addDataDialog.show(
                fragmentManager, AddDataDialogFragment.TAG
            )
        }
    }


    private fun bindServiceWithPermission() {
        if (checkPermission()) {
            connection = object: ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as StepCounterService.StepCounterBinder
                    stepCounterService = binder.getService()
                    stepCounterService?.setTaskViewModel(taskViewModel)
                    stepCounterService?.setAppActive(true)
                    isBound = true
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    stepCounterService = null
                    isBound = false
                }
            }
            if (taskViewModel.isStepItemAdded.value == true) {
                startStepCounterService()
            }

        } else {
            requestPermission()
        }
    }

    @SuppressLint("InlinedApi")
    fun checkPermission() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("InlinedApi")
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            REQUEST_ACTIVITY_RECOGNITION
        )
    }


    private fun startStepCounterService() {
        Log.d("debug", "Starting service bind")
        val serviceIntent = Intent(this, StepCounterService::class.java)
        //Android 8.0 requirements
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(serviceIntent)
        }
        else {
            startService(serviceIntent)
        }
        connection?.let {
            bindService(serviceIntent, it, Context.BIND_AUTO_CREATE)
        }
    }

    private fun stopStepCounterService() {
        if (isBound) {
            connection?.let { unbindService(it) }
            isBound = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // All permissions granted, bind the service
            bindServiceWithPermission()
        } else {
            // Handle permission denial (e.g., show a message or disable functionality)
            Toast.makeText(this, "Permissions denied. Cannot track steps.", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkExistingItems() {
        Log.d("debug", "checking existing items")
        val hasStepItems = adapter.currentList.any { it.habitTotalStepCount != 0}
        taskViewModel.setStepItemAdded(hasStepItems)
    }

    private fun retrievePendingSteps(habits: List<Habit>) {
//        Log.d("debug", "Processing pending steps for habits: $habits")
        if (habits.isNotEmpty()) {
            stepCounterService?.pushPendingSteps() // Process steps
        }
    }

    override fun onStart() {
        super.onStart()
        taskViewModel.isStepItemAdded.observe(this) { isAdded ->
            if (isAdded) {
                startStepCounterService()
            } else {
                stopStepCounterService()
            }
        }
//        startStepCounterService()
        taskViewModel.allHabits.observe(this as LifecycleOwner) { habits ->
            habits?.let{ adapter.submitList(it)}
//            Log.d("debug", "Habits observed: $habits")
            if (habits != null && habits.isNotEmpty()) {
                if (!pendingStepsProcessed) {
                    retrievePendingSteps(habits)
                    pendingStepsProcessed = true
                }
            } else {
                Log.d("debug", "Habits not yet loaded")
            }
        }

        taskViewModel.stepGoalReached.observe(this) { event ->
            event.getContentIfNotHandled()?.let { totalSteps ->
                if (taskViewModel.isStepItemAdded.value == true) {
                    StepNotificationMaker.createNotificationChannel(applicationContext)
                    StepNotificationMaker.sendGoalNotification(applicationContext, totalSteps)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (taskViewModel.isStepItemAdded.value == true){
            stepCounterService?.setAppActive(true)
        }
    }

    override fun onStop() {
        super.onStop()
        stepCounterService?.setAppActive(false)
        stopStepCounterService()
    }

    override fun onDestroy() {
        stepCounterService?.setAppActive(false)
        stopStepCounterService()
        super.onDestroy()
    }

    companion object {
        const val REQUEST_ACTIVITY_RECOGNITION = 1001
    }
}