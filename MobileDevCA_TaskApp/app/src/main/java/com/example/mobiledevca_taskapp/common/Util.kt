//
package com.example.mobiledevca_taskapp.common

import android.content.Context

object Utils {

    //SharedPreferences file
    private const val PREFS_NAME = "ActivityPrefs"
    //Key that the name of last activity is saved under
    private const val LAST_ACTIVITY_KEY = "last_activity"

    //Sets string reference to last activity used
    @JvmStatic
    fun setLastActivity(context: Context, activityName: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(LAST_ACTIVITY_KEY, activityName).apply()
    }

    //Returns string to last activity used
    @JvmStatic
    fun getLastActivity(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LAST_ACTIVITY_KEY, "")
    }

}
