package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import androidx.room.TypeConverter
import com.example.mobiledevca_taskapp.taskDatabase.entities.Task
import org.json.JSONArray
import org.json.JSONObject

//Room has no idea how to add a list of items to an entity table, so we made a custom data type converter using JSON
class TaskConverter {

    @TypeConverter
    fun fromTaskList(tasks: List<Task>): String {
        val jsonArray = JSONArray()
        for (task in tasks) {
            val jsonObject = JSONObject()
            jsonObject.put("taskId", task.taskId)
            jsonObject.put("taskName", task.taskName)
            jsonObject.put("description", task.description)
            jsonObject.put("isChecked", task.isChecked)
            jsonObject.put("time", task.time)
            jsonObject.put("date", task.date)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toTaskList(data: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val jsonArray = JSONArray(data)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val task = Task(
                taskId = jsonObject.getInt("taskId"),
                taskName = jsonObject.getString("taskName"),
                description = jsonObject.getString("description"),
                isChecked = jsonObject.getBoolean("isChecked"),
                time = jsonObject.getString("time"),
                date = jsonObject.getString("date")
            )
            tasks.add(task)
        }
        return tasks
    }
}