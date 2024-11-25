package com.example.mobiledevca_taskapp.recyclerViewUnused

class Tasks
{
    var Task_Name: String = ""
    var Description: String = ""

    fun set_Card(Name: String, Description: String)
    {
        this.Task_Name = Name
        this.Description = Description
    }

    fun get_name(): String
    {
        return Task_Name
    }

    fun get_description(): String
    {
        return Description
    }
}