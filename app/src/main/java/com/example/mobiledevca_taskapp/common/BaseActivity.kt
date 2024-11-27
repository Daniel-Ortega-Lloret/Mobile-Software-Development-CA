// This is an abstract class that all Activities of the app inherit from
// Add any logic that all classes need to implement here
package com.example.mobiledevca_taskapp.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledevca_taskapp.HabitsActivity
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.ScheduleActivity
import com.example.mobiledevca_taskapp.TasksActivity
import com.example.mobiledevca_taskapp.databinding.ActivityBaseBinding
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var taskViewModel: TaskViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted){
                Log.d("debug", "notifications allowed")
            }
            else {
                openNotificationSettings()
                Log.d("debug", "notifications not allowed")
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Creates top action bar with buttons
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Before setting up navigation view, set up the nav view drawer
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        //Set up the navigation view
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            handleMenuItemClick(menuItem, drawerLayout)
            true
        }

        //Set up View model, will dynamically create one if class has an implementation
        val app = application as TaskAppApplication
        val factory = TaskViewModelFactory(app, app.applicationScope)
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)

        requestNotificationPermissions()
    }

    //Sets up content layout for each activity
    //Sets up actionBar title string
    protected fun setActivityContent(layoutResID: Int, layoutName: String) {
        val frameLayout = binding.contentFrame

        // Clear previous views from content frame to prevent overlaying or reuse issues
        if (frameLayout.childCount > 0){
            frameLayout.removeAllViewsInLayout()
        }

        // Inflate the provided layout resource into the content frame
        layoutInflater.inflate(layoutResID, frameLayout, true)

        // Set ActionBar title for current activity
        supportActionBar?.title = layoutName
    }

    //Called after onCreate to sync the state of the hamburger button
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    // Handle the hamburger icon as a tap to open the drawer
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun requestNotificationPermissions() {
        //From API 13 onward we need permissions to send notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("debug", "notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    //This handles the logic of when a user taps a menu option
    private fun handleMenuItemClick(menuItem: MenuItem, drawerLayout: DrawerLayout) {
        drawerLayout.closeDrawer(GravityCompat.START)

        when (menuItem.itemId) {
            R.id.nav_tasks -> {
                openActivity(TasksActivity::class.java)
            }

            R.id.nav_schedule -> {
                openActivity(ScheduleActivity::class.java)
            }

            R.id.nav_habits -> {
                openActivity(HabitsActivity::class.java)
            }
        }
    }

    //Opens given activity unless user is already in that activity
    //Only loads one new instance of each, if its already in the stack it just brings it to the front
    private fun openActivity(activityClass: Class<*>) {
        if (activityClass != this::class.java) {
            startActivity(Intent(this, activityClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
        }
    }

    //Handles the 3 dots icon menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_drawer, menu)
        return true
    }

    //Updates highlighted menu item
    override fun onResume() {
        super.onResume()
        updateCheckedNavigationItem()
    }

    //Checks ActivityPrefs for the current activity and sets highlight to that
    private fun updateCheckedNavigationItem() {
        val lastActivity = this::class.java.simpleName

        val navView: NavigationView = findViewById(R.id.nav_view)

        when (lastActivity) {
            "TasksActivity" -> navView.setCheckedItem(R.id.nav_tasks)
            "ScheduleActivity" -> navView.setCheckedItem(R.id.nav_schedule)
            "HabitsActivity" -> navView.setCheckedItem(R.id.nav_habits)
        }
    }

}