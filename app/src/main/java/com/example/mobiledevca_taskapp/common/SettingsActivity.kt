package com.example.mobiledevca_taskapp.common

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.mobiledevca_taskapp.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // The root_preferences saves all settings changes for us
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val SwichPreference = findPreference<SwitchPreferenceCompat?>("Dark_Mode_Switch")

            // Check If its on dark / light mode and changes the switch accordingly
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val sharedPreferences = preferenceManager.sharedPreferences
            val CheckSwitch = sharedPreferences?.getBoolean("Dark_Mode_Switch", false)

            if (CheckSwitch != null && CheckSwitch == true)
            {
                SwichPreference?.isChecked = true
            }
            else
            {
                SwichPreference?.isChecked = false
            }
            /*
            when(currentMode)
            {
                // Night Mode Enabled
                AppCompatDelegate.MODE_NIGHT_YES ->
                {
                    SwichPreference?.isChecked = true
                }

                // Night Mode Disabled
                AppCompatDelegate.MODE_NIGHT_NO ->
                {
                    SwichPreference?.isChecked = false
                }

                // Its not specified, So We Must Find Out If Its Light or Dark
                AppCompatDelegate.MODE_NIGHT_UNSPECIFIED ->
                {
                    if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
                    {
                        SwichPreference?.isChecked = true
                    }
                }
                // If There is a problem its probably cause i didnt check for sys default (= follow_system)
            } */


            // Listen for clicks
            SwichPreference?.setOnPreferenceClickListener {
                if (SwichPreference.isChecked)
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Toast.makeText(context, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
                    true
                }
                else
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Toast.makeText(context,"Dark Mode Disabled", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            // We Change the theme based on this if statement
                    // Probably wont need, if i can access root preferences from other files
//
//            val CheckSwitch = sharedPreferences?.getBoolean("Dark_Mode_Switch", false)
//            if (CheckSwitch == true) {
//                // If Switch is on
//            } else {
//
//            }

        }
    }
}