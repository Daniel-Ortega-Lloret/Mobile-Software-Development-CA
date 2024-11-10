package com.example.mobiledevca_taskapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        _calendarBtn = findViewById<Button>(R.id.CalendarBtn)
        _calendarBtn?.setOnClickListener{
            val intent = Intent(this, Calendar::class.java)
            startActivity(intent)
        }
    }

    private var _calendarBtn: Button? = null
}
