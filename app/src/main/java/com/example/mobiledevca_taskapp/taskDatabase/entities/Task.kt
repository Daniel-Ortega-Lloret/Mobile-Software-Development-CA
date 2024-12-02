package com.example.mobiledevca_taskapp.taskDatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.sql.Time

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) var taskId: Int = 0,
    val taskName: String,
    val description: String,
    var isChecked: Boolean = false,
    var time: String,
    var date: String,
    var position: Int = 0
)