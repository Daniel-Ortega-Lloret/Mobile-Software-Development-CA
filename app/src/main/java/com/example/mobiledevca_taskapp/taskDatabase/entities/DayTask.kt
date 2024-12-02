package com.example.mobiledevca_taskapp.taskDatabase.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "DayTask",
    primaryKeys = ["dayId", "taskId"],
    foreignKeys = [
        ForeignKey(entity = Day::class, parentColumns = ["dayId"], childColumns = ["dayId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Task::class, parentColumns = ["taskId"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class DayTask(
    val dayId: Int,
    val taskId: Int
)
