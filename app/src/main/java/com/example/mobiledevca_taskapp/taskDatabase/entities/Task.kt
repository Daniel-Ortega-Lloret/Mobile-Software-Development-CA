package com.example.mobiledevca_taskapp.taskDatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) var taskId: Int = 0,
    val taskName: String,
    val description: String,
    var isChecked: Boolean = false
)