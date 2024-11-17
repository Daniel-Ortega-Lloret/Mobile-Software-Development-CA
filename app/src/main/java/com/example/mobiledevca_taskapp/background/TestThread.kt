//
package com.example.mobiledevca_taskapp.background

import android.os.Bundle
import android.os.Message
import com.example.mobiledevca_taskapp.common.BaseActivity

class TestThread(handler: ThreadHandler, activity: BaseActivity) : Thread() {

    private var _handler: ThreadHandler = handler
    private var _activity: BaseActivity = activity
    private val _activityName = activity.javaClass.name

    override fun run() {
        val preloadResult = preloadActivityResources(_activityName)

        val m: Message = _handler.obtainMessage()
        m.obj = preloadResult
        m.data = Bundle().apply { putString("activityName", _activityName)}
        _handler.sendMessage(m)
    }

    //Use this function to pre-load network calls, taskDatabase access, etc, etc...
    private fun preloadActivityResources(activityName: String) : Any {
        // Return any result required here
        return "Resources loaded"
    }
}