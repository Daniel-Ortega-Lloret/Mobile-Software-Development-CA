// Tasks Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.mobiledevca_taskapp.common.BaseActivity
import com.example.mobiledevca_taskapp.recyclerView.RecyclerItem
import com.example.mobiledevca_taskapp.taskDatabase.TaskApplication

import com.example.mobiledevca_taskapp.taskDatabase.TaskListAdapter
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModel
import com.example.mobiledevca_taskapp.taskDatabase.TaskViewModelFactory
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task

class TasksActivity : BaseActivity() {
    private lateinit var _recyclerview: RecyclerView
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as TaskApplication).repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_tasks, getString(R.string.menu_tasks))

        _recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = TaskListAdapter()
        _recyclerview.adapter = adapter
        // Set a linear layout manager on the recycler view then generate an adapter and attach it
        _recyclerview.layoutManager = LinearLayoutManager(this)

        taskViewModel.allTasks.observe(this as LifecycleOwner) {tasks ->
            tasks?.let { adapter.submitList(it) }
        }

        val Add_Card_Button: Button = findViewById<Button>(R.id.Add_Card)
        Add_Card_Button.setOnClickListener {
            inputDialog()
        }

    }

    fun inputDialog()
    {
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)

        //Inflate Layout
        val inflate = layoutInflater
        val dialogLayout = inflate.inflate(R.layout.add_card_dialog, null)

        // Title For Card
        builder.setTitle("Enter Task Details")
        // Task Name
        val Card_Name = dialogLayout.findViewById<EditText>(R.id.Dialog_Task_Name)
        Card_Name.hint = "Card Name"

        // Task Description
        val Card_Description = dialogLayout.findViewById<EditText>(R.id.Dialog_Task_Description)
        Card_Description.hint = "Card Description"

        builder.setView(dialogLayout)


        // Confirm Button To Accept Text Fields
        builder.setPositiveButton("Confirm", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                val task = Task(0, Card_Name.text.toString(), Card_Description.text.toString())
                taskViewModel.insert(task)
            }
        })

        // Cancel Button To Dismiss Text Fields
        builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                // Do Nothing for now
            }
        })

        // Build The Dialog And Show It
        var dialog: AlertDialog = builder.create()
        dialog.show()
    }

    companion object {
        const val EXTRA_REPLY = "com.example.android.taskList.sql.REPLY"
    }

}