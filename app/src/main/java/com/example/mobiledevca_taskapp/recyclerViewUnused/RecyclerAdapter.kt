package com.example.mobiledevca_taskapp.recyclerViewUnused

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobiledevca_taskapp.R

class RecyclerAdapter(val context: Context, var ri_arraylist: ArrayList<RecyclerItem>) :
RecyclerView.Adapter<RecyclerAdapter.ItemHolder>()
{
    // Private Fields
    private val _context: Context = context
    private val _ri_arraylist: ArrayList<RecyclerItem> = ri_arraylist

    // This creates a viewholder for the recycler adapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder
    {
        /* get access to the layout inflater and inflate a layout
                for one of our recycler view items */
        return ItemHolder(LayoutInflater.from(_context).inflate(R.layout.recycler_item, parent, false))
    }

    // Binds an item in our arraylist to a view holder so it can be displayed
    override fun onBindViewHolder(holder: ItemHolder, position: Int)
    {
        // Gets Item At Current Position
        val item: RecyclerItem = _ri_arraylist.get(position)

        // Set The Number And Text On The View Holder
        holder._task_name.setText(item.get_name())
        holder._task_description.setText(item.get_description())
    }

    // Returns number of items in recyclerAdapter
    override fun getItemCount(): Int
    {
        return _ri_arraylist.size
    }

    // Implements a view holder for an item in the list
    class ItemHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener
    {
        private var _view: View = v
        private var _recycler_item: RecyclerItem? = null
        lateinit var _task_name: TextView
        lateinit var _task_description: TextView
        // Called To Initialise The Object
        init {
            // pull references from the layout for the text views
            _task_name = _view.findViewById<TextView>(R.id.Task_Name)
            // Set A Listener For Clicks On This View Holder
            _view.setOnClickListener(this)
        }

        override fun onClick(p0: View?)
        {
            Log.i("RecyclerView", "Clicked on item" + _task_name)
        }
    }
}