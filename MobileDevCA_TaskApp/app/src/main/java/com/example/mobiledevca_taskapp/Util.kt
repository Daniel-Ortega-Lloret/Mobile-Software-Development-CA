package com.example.mobiledevca_taskapp

import android.content.Context

object Utils {

    private const val PREFS_NAME = "ActivityPrefs"
    private const val LAST_ACTIVITY_KEY = "last_activity"

    @JvmStatic
    fun setLastActivity(context: Context, activityName: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(LAST_ACTIVITY_KEY, activityName).apply()
    }

    @JvmStatic
    fun getLastActivity(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAST_ACTIVITY_KEY, "")
    }
}
