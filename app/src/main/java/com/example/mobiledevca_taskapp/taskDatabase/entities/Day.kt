package com.example.mobiledevca_taskapp.taskDatabase.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["dayNumber", "month", "year"], unique = true)])
data class Day(
    @PrimaryKey(autoGenerate = true) var dayId: Int = 0,
    var dayName: String,
    var dayNumber: Int,
    var timeSlots: List<TimeSlot> = emptyList(),
    var month: Int,
    var year: Int
)
