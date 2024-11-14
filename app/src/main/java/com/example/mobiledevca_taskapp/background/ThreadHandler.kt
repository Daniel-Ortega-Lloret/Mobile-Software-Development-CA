package com.example.mobiledevca_taskapp.background

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.mobiledevca_taskapp.common.BaseActivity

class ThreadHandler(ba: BaseActivity): Handler(Looper.myLooper()!!) {
    private var _ba: BaseActivity = ba

    // Call UI thread functions in here
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        // e.g: _ma.updateUI(msg.arg1)
    }
}