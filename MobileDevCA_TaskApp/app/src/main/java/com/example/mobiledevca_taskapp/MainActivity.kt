package com.example.mobiledevca_taskapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val CalenderBtn = findViewById<View>(R.id.CalenderBtn)
        CalenderBtn.setOnClickListener{
            val intent = Intent(this, Calender::class.java)
            startActivity(intent)
        }
    }
}
