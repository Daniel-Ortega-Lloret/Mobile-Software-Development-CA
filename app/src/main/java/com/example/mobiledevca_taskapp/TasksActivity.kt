// Tasks Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.taskDatabase.TaskApplication

import com.example.mobiledevca_taskapp.taskDatabase.TaskListAdapter
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TasksActivity : BaseActivity() {
    private val newTaskActivityRequestCode = 1
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as TaskApplication).repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_tasks, getString(R.string.menu_tasks))

        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)
        val adapter = TaskListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener{
            //Add dialog here
        }

        taskViewModel.allTasks.observe(this) { task ->
            task.let { adapter.submitList(it)}
        }

    }

}