package com.example.mobiledevca_taskapp

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mobiledevca_taskapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarAppDrawer.toolbar)
        supportActionBar?.title = getString(R.string.menu_tasks)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.appBarAppDrawer.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            handleMenuItemClick(menuItem)
            true
        }

    }

    //Use this to change/add stuff to the side menu
    //Only loads one new instance of each, if its already in the stack it just brings it to the front
    private fun handleMenuItemClick(menuItem: MenuItem) {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        drawerLayout.closeDrawer(binding.navView)

        val sharedPreferences = getSharedPreferences("ActivityPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val intent = when (menuItem.itemId) {
            R.id.nav_tasks -> {
                Utils.setLastActivity(this, "TasksActivity")
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
            }

            R.id.nav_schedule -> {
                Utils.setLastActivity(this, "Calendar")
                Intent(this, Calendar::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
            }

            R.id.nav_habits -> {
                Utils.setLastActivity(this, "HabitsActivity")
                Intent(this, HabitsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
            }

            else -> null
        }

        intent?.let {
            startActivity(it)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_drawer, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        return if (drawerLayout.isDrawerOpen(binding.navView)) {
            drawerLayout.closeDrawer(binding.navView)
            true
        } else {
            super.onSupportNavigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        val lastActivity = Utils.getLastActivity(this)
        val navView: NavigationView = binding.navView

        when (lastActivity) {
            "TasksActivity" -> navView.setCheckedItem(R.id.nav_tasks)
            "Calendar" -> navView.setCheckedItem(R.id.nav_schedule)
            "HabitsActivity" -> navView.setCheckedItem(R.id.nav_habits)
        }
    }


}







