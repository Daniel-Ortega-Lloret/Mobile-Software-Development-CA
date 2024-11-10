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
import com.example.mobiledevca_taskapp.databinding.ActivityHabitBinding
import com.google.android.material.navigation.NavigationView

class HabitsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHabitBinding
    private var _habitCalendarBtn: Button? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarAppDrawer.toolbar)
        supportActionBar?.title = getString(R.string.menu_habits)

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

        _habitCalendarBtn = findViewById<Button>(R.id.habits_calendarButton)

        _habitCalendarBtn?.setOnClickListener({
            val intent = Intent(this, Calendar::class.java)
            startActivity(intent)
        })
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        drawerLayout.closeDrawer(binding.navView)

        val intent = when (menuItem.itemId) {
            R.id.nav_tasks -> Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            R.id.nav_schedule -> Intent(this, Calendar::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            R.id.nav_habits -> Intent(this, HabitsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
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

}