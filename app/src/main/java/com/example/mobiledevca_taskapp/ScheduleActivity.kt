//Calendar Activity - Extends BaseActivity

package com.example.mobiledevca_taskapp

import android.os.Bundle
import com.example.mobiledevca_taskapp.common.BaseActivity

class ScheduleActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActivityContent(R.layout.activity_schedule, getString(R.string.menu_schedule))
    }
}