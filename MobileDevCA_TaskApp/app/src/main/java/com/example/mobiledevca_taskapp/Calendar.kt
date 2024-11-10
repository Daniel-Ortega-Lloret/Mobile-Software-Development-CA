package com.example.mobiledevca_taskapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Calendar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)

        _habitBtn = findViewById<Button>(R.id.habits_button)
        _habitBtn?.setOnClickListener({
            val intent = Intent(this, HabitsActivity::class.java)
            startActivity(intent)
        })
    }

    private var _habitBtn: Button? = null
}