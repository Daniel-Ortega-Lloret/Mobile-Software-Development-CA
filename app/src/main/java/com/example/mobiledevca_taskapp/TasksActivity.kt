// Tasks Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.fragments.AddDataDialogFragment
import com.example.mobiledevca_taskapp.fragments.UpdateDataDialogFragment
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory

import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskListAdapter
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task

class TasksActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager
    private lateinit var id : String
    private lateinit var name : String
    private lateinit var taskAppViewModel : TaskViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For passing application to adapter to use for dao (changcheckbox)
        val factory = TaskViewModelFactory(application as TaskAppApplication) // This casts it to TaskAppApplication
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)


        setActivityContent(R.layout.activity_tasks, getString(R.string.menu_tasks))

        _recyclerview = findViewById(R.id.recyclerview)
        fragmentManager = supportFragmentManager

        // Passing Update Dialog
        val adapter = TaskListAdapter(supportFragmentManager, taskViewModel)
        _recyclerview.adapter = adapter
        // Set a linear layout manager on the recycler view then generate an adapter and attach it
        _recyclerview.layoutManager = LinearLayoutManager(this)


        taskViewModel.allTasks.observe(this as LifecycleOwner) {tasks ->
            tasks?.let { adapter.submitList(it) }
        }



        id = getString(R.string.tasks_id)
        name = getString(R.string.tasks_name)

        val Add_Card_Button: Button = findViewById(R.id.Add_Card)
        Add_Card_Button.setOnClickListener {
            val addDataDialog = AddDataDialogFragment.newInstance(id, name)
            addDataDialog.show(
                fragmentManager, AddDataDialogFragment.TAG
            )
        }

        val resetButton: Button = findViewById(R.id.Delete_Card)
        resetButton.setOnClickListener{
            taskViewModel.deleteAllTasks()
            Toast.makeText(this, "All tasks deleted", Toast.LENGTH_SHORT).show()
        }

    }
}