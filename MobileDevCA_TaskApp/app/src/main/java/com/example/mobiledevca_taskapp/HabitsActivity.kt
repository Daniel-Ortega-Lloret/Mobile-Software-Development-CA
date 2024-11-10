package com.example.mobiledevca_taskapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HabitsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_habit)

        _habitCalendarBtn = findViewById<Button>(R.id.habits_calendarButton)

        _habitCalendarBtn?.setOnClickListener({
            val intent = Intent(this, Calendar::class.java)
            startActivity(intent)
        })
    }

    private var _habitCalendarBtn: Button? = null;
}