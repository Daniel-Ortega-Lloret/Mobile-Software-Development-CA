package com.example.mobiledevca_taskapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import java.util.prefs.Preferences

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
//            val sharedPreferences = preferenceManager.sharedPreferences
//            val CheckSwitch = sharedPreferences?.getBoolean("Dark_Mode_Switch", false)
//            if (CheckSwitch == true) {
//                // If Switch is on
//            } else {
//
//            }

        }
    }
}