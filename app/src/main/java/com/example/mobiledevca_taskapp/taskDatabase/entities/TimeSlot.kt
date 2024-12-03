package com.example.mobiledevca_taskapp.taskDatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimeSlot(
    @PrimaryKey(autoGenerate = true) var timeSlotId: Int = 0,
    var time: String,
    var tasks: List<Task> = emptyList()
)