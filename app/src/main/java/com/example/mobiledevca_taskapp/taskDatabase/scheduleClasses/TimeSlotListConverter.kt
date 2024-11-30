package com.example.mobiledevca_taskapp.taskDatabase.scheduleClasses

import androidx.room.TypeConverter
import com.example.mobiledevca_taskapp.taskDatabase.entities.TimeSlot
import org.json.JSONArray
import org.json.JSONObject

//Same as the task list adapter but for timeslot lists
class TimeSlotListConverter {
    @TypeConverter
    fun fromTimeSlotList(timeSlots: List<TimeSlot>): String {
        val jsonArray = JSONArray()
        for (timeSlot in timeSlots) {
            val jsonObject = JSONObject()
            jsonObject.put("timeSlotId", timeSlot.timeSlotId)
            jsonObject.put("time", timeSlot.time)
            jsonObject.put("tasks", TaskConverter().fromTaskList(timeSlot.tasks))
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toTimeSlotList(data: String): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        val jsonArray = JSONArray(data)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val tasksJson = jsonObject.getString("tasks")
            val tasks = TaskConverter().toTaskList(tasksJson)
            val timeSlot = TimeSlot(
                timeSlotId = jsonObject.getInt("timeSlotId"),
                time = jsonObject.getString("time"),
                tasks = tasks
            )
            timeSlots.add(timeSlot)
        }
        return timeSlots
    }
}
