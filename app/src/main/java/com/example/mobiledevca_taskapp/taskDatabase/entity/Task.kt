package com.example.mobiledevca_taskapp.taskDatabase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey val taskId: Int
)