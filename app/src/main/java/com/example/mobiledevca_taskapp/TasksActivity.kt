// Tasks Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.common.BaseActivity

class TasksActivity : BaseActivity() {

    private val Card_Array = mutableListOf<Tasks>()
    // Private Fields
    private lateinit var _recyclerview: RecyclerView
    private var _rl_arraylist: ArrayList<RecyclerItem> = ArrayList<RecyclerItem>()
    private var _count: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActivityContent(R.layout.activity_tasks, getString(R.string.menu_tasks))

        _recyclerview = findViewById<RecyclerView>(R.id.recyclerview)

        // Set a linear layout manager on the recycler view then generate an adapter and attach it
        _recyclerview.layoutManager = LinearLayoutManager(this)
        var recycler_adapter: RecyclerAdapter = RecyclerAdapter(this, _rl_arraylist)
        _recyclerview.adapter = recycler_adapter


        val Add_Card_Button: Button = findViewById<Button>(R.id.Add_Card)
        Add_Card_Button.setOnClickListener {

            inputDialog(Card_Array)
            if (Card_Array.size > 0)
            {

            }


        }
    }




    fun inputDialog(Card_Array: MutableList<Tasks>)
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
                // We Instantiate A Task If We Confirm
                var Task = Tasks()
                Task.set_Card(Card_Name.getText().toString(), Card_Description.getText().toString())
                _rl_arraylist.add(RecyclerItem(Task))
                _recyclerview.adapter?.notifyItemInserted(_rl_arraylist.size - 1)
                //Card_Array.add(Task)
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


}