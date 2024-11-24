// This is an abstract class that all Activities of the app inherit from
// Add any logic that all classes need to implement here
package com.example.mobiledevca_taskapp.common

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledevca_taskapp.HabitsActivity
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.ScheduleActivity
import com.example.mobiledevca_taskapp.SettingsActivity
import com.example.mobiledevca_taskapp.TasksActivity
import com.example.mobiledevca_taskapp.background.TestThread
import com.example.mobiledevca_taskapp.background.ThreadHandler
import com.example.mobiledevca_taskapp.databinding.ActivityBaseBinding
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var handler: ThreadHandler
    lateinit var taskViewModel: TaskViewModel

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

        //Use this to run background tasks for each activity
        //Handler also instantiates the activity
        handler = ThreadHandler(this)

        TestThread(handler, this).start()

        //Set up View model, will dynamically create one if class has an implementation
        val app = application as TaskAppApplication
        val factory = TaskViewModelFactory(app)
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)
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

    //This handles the logic of when a user taps a menu option
    private fun handleMenuItemClick(menuItem: MenuItem, drawerLayout: DrawerLayout) {
        drawerLayout.closeDrawer(GravityCompat.START)

        when (menuItem.itemId) {
            R.id.nav_tasks -> {
                openActivity(TasksActivity::class.java)
            }

            R.id.nav_settings -> {
                openActivity(SettingsActivity::class.java)
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