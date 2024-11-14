package com.example.mobiledevca_taskapp.background

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.mobiledevca_taskapp.common.BaseActivity

class ThreadHandler(ba: BaseActivity): Handler(Looper.myLooper()!!) {
    private var _ba: BaseActivity = ba

    // Call UI thread functions in here
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        //Start or display activity on the main thread here
        val result = msg.obj // Example of return object from thread
        val activityName = msg.data.getString("activityName")
        Log.d("ThreadHandler", "Preload result received from $activityName")

        // e.g: _ma.updateUI(msg.arg1)
    }
}