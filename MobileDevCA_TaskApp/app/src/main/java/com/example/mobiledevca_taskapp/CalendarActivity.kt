//Calendar Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.os.Bundle
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.databinding.ActivityCalendarBinding

class CalendarActivity : BaseActivity() {
    override lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarAppDrawer.toolbar)
        supportActionBar?.title = getString(R.string.menu_schedule)

        setUpDrawer(binding.navView, binding.drawerLayout)
    }
}