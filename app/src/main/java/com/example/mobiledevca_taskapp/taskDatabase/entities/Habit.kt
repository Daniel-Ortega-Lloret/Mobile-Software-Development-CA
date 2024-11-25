package com.example.mobiledevca_taskapp.taskDatabase.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Habit(
    @PrimaryKey(autoGenerate = true) var habitId: Int = 0,
    val habitName: String,
    val habitReset: Int? = 0,
    var habitCountCheck: Int? = 0,
    var habitSwitch: Int? = 0,
    var habitCount: Int? = 0,
    val habitStepCount: Int? = null,
    val habitTotalStepCount: Int? = null
)