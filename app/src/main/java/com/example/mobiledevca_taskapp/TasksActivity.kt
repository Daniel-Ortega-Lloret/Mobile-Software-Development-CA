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
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.fragments.AddDataDialogFragment
import com.example.mobiledevca_taskapp.fragments.UpdateDataDialogFragment
import com.example.mobiledevca_taskapp.taskDatabase.TaskAppApplication
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory

import com.example.mobiledevca_taskapp.taskDatabase.taskClasses.TaskListAdapter
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import java.util.Date

class TasksActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private lateinit var fragmentManager: FragmentManager
    // For passing to recycler view

    private lateinit var id : String
    private lateinit var name : String
    private lateinit var taskAppViewModel : TaskViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For passing application to adapter to use for dao (changcheckbox)
        val app = application as TaskAppApplication
        val factory = TaskViewModelFactory(app, app.applicationScope) // This casts it to TaskAppApplication
        taskAppViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)


        setActivityContent(R.layout.activity_tasks, getString(R.string.menu_tasks))

        _recyclerview = findViewById(R.id.recyclerview)
        fragmentManager = supportFragmentManager

        // Passing Update Dialog
        val adapter = TaskListAdapter(supportFragmentManager, taskViewModel)
        _recyclerview.adapter = adapter
        // Set a linear layout manager on the recycler view then generate an adapter and attach it
        _recyclerview.layoutManager = LinearLayoutManager(this)

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

                        adapter.moveItem(from, to)
                        // Tell adapter to render the update
                        //adapter.notifyItemMoved(from, to)
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        // If Swiped
                    }
                }
            ItemTouchHelper(simpleItemTouchCallback)
        }
        itemTouchHelper.attachToRecyclerView(_recyclerview)

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