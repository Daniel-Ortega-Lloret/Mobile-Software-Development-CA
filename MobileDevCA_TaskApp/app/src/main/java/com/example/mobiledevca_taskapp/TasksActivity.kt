// Tasks Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.os.Bundle
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.databinding.ActivityTasksBinding

class TasksActivity : BaseActivity() {
    override lateinit var binding: ActivityTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarAppDrawer.toolbar)
        supportActionBar?.title = getString(R.string.menu_tasks)

        setUpDrawer(binding.navView, binding.drawerLayout)
    }

}