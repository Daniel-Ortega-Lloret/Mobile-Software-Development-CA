// Tasks Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.fragments.AddDataDialogFragment
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory

import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskListAdapter


class TasksActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager
    private lateinit var taskAppViewModel : TaskViewModel

    // To Figure out what type was passed to dialog (habit / task)
    private lateinit var id : String
    private lateinit var name : String


    private lateinit var adapter : TaskListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For passing application to adapter to use for dao (changcheckbox)
        val app = application as TaskAppApplication
        val factory = TaskViewModelFactory(app, app.applicationScope) // This casts it to TaskAppApplication
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)


        setActivityContent(R.layout.activity_tasks, getString(R.string.menu_tasks))

        _recyclerview = findViewById(R.id.recyclerview)
        fragmentManager = supportFragmentManager

        // Passing fragment manager for dialogs, taskViewModel for dao changes
        adapter = TaskListAdapter(supportFragmentManager, taskViewModel)
        _recyclerview.adapter = adapter

        // Set a linear layout manager on the recycler view then generate an adapter and attach it
        _recyclerview.layoutManager = LinearLayoutManager(this)
        _recyclerview.itemAnimator = null   // Stops Glitchy animations when moving tasks

        // For Moving The Items
        val itemTouchHelper by lazy {
            val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
                {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val adapter = recyclerView.adapter as TaskListAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition

                        // Updates Lists. Its In TaskAdapter
                        adapter.moveItem(from, to)
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        // For Swiping Functionality
                    }
                }
            ItemTouchHelper(simpleItemTouchCallback)
        }
        itemTouchHelper.attachToRecyclerView(_recyclerview)



        id = getString(R.string.tasks_id)
        name = getString(R.string.tasks_name)

        val Add_Card_Button: Button = findViewById(R.id.Add_Card)
        Add_Card_Button.setOnClickListener {
            val addDataDialog = AddDataDialogFragment.newInstance(id, name)
            addDataDialog.show(
                fragmentManager, AddDataDialogFragment.TAG
            )
        }
    }

    override fun onResume() {
        super.onResume()

        val taskActivityLabel = findViewById<TextView>(R.id.taskActivityLabel)

        taskViewModel.allTasks.observe(this as LifecycleOwner) {tasks ->
            if (tasks.isNullOrEmpty()){
                taskActivityLabel.visibility = View.VISIBLE
            }
            else{
                taskActivityLabel.visibility = View.GONE
            }
            tasks.let { adapter.submitList(it) }
        }
    }
}