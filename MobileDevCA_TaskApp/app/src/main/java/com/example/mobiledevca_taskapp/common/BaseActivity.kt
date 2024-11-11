package com.example.mobiledevca_taskapp.common

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mobiledevca_taskapp.Calendar
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
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

    //Use this to change/add stuff to the side menu
    //Only loads one new instance of each, if its already in the stack it just brings it to the front
    private fun handleMenuItemClick(menuItem: MenuItem, drawerLayout: DrawerLayout) {
        drawerLayout.closeDrawer(GravityCompat.START)

        when (menuItem.itemId) {
            R.id.nav_tasks -> {
                Utils.setLastActivity(this, "TasksActivity")
                openActivity(TasksActivity::class.java)
            }

            R.id.nav_schedule -> {
                Utils.setLastActivity(this, "Calendar")
                openActivity(Calendar::class.java)
            }

            R.id.nav_habits -> {
                Utils.setLastActivity(this, "HabitsActivity")
                openActivity(HabitsActivity::class.java)
            }
        }
    }

    //Opens given activity unless user is already in that activity
    private fun openActivity(activityClass: Class<*>) {
        if (activityClass != this::class.java) {
            startActivity(Intent(this, activityClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_drawer, menu)
        return true
    }

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

    override fun onResume() {
        super.onResume()
        updateCheckedNavigationItem()
    }

    private fun updateCheckedNavigationItem() {
        val lastActivity = Utils.getLastActivity(this)

        val navView: NavigationView = findViewById(R.id.nav_view)

        when (lastActivity) {
            "TasksActivity" -> navView.setCheckedItem(R.id.nav_tasks)
            "Calendar" -> navView.setCheckedItem(R.id.nav_schedule)
            "HabitsActivity" -> navView.setCheckedItem(R.id.nav_habits)
        }
    }

}