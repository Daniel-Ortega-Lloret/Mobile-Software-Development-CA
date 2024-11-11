package com.example.mobiledevca_taskapp

import android.os.Bundle
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.databinding.ActivityHabitBinding

class HabitsActivity : BaseActivity() {
    override lateinit var binding: ActivityHabitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarAppDrawer.toolbar)
        supportActionBar?.title = getString(R.string.menu_habits)

        setUpDrawer(binding.navView, binding.drawerLayout)

    }

}