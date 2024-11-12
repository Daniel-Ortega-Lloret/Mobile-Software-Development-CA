// This is an abstract class that all Activities of the app inherit from
// Add any logic that all classes need to implement here
package com.example.mobiledevca_taskapp.common

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mobiledevca_taskapp.CalendarActivity
import com.example.mobiledevca_taskapp.HabitsActivity
import com.example.mobiledevca_taskapp.R
import com.example.mobiledevca_taskapp.TasksActivity
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {
    protected abstract val binding : androidx.viewbinding.ViewBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //Sets up navigation and settings buttons
    protected fun setUpDrawer(navView: NavigationView, drawerLayout: DrawerLayout) {
        //Creates top action bar with buttons
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Hamburger icon logic
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            handleMenuItemClick(menuItem, drawerLayout)
            true
        }
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
                Utils.setLastActivity(this, "TasksActivity")
                openActivity(TasksActivity::class.java)
            }

            R.id.nav_schedule -> {
                Utils.setLastActivity(this, "CalendarActivity")
                openActivity(CalendarActivity::class.java)
            }

            R.id.nav_habits -> {
                Utils.setLastActivity(this, "HabitsActivity")
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

    //Lets user touch and hold to draw the menu to the right
    override fun onSupportNavigateUp(): Boolean {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        return if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        else {
            super.onSupportNavigateUp()
        }
    }

    //Updates highlighted menu item
    override fun onResume() {
        super.onResume()
        updateCheckedNavigationItem()
    }

    //Checks ActivityPrefs for the current activity and sets highlight to that
    private fun updateCheckedNavigationItem() {
        val lastActivity = Utils.getLastActivity(this)

        val navView: NavigationView = findViewById(R.id.nav_view)

        when (lastActivity) {
            "TasksActivity" -> navView.setCheckedItem(R.id.nav_tasks)
            "CalendarActivity" -> navView.setCheckedItem(R.id.nav_schedule)
            "HabitsActivity" -> navView.setCheckedItem(R.id.nav_habits)
        }
    }

}