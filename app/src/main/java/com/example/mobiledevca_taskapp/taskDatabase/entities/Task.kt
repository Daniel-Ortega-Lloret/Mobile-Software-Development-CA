package com.example.mobiledevca_taskapp.taskDatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey val taskId: Int,
    val taskName: String
)