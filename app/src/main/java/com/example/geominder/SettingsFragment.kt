package com.example.geominder

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

//UNFINISHED
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<SwitchPreferenceCompat>("notifications")
            ?.setOnPreferenceChangeListener { _, newValue ->
                Log.d("Preferences", "Notifications enabled: $newValue")
                true // Return true if the event is handled.
            }

        findPreference<Preference>("feedback")
            ?.setOnPreferenceClickListener {
                Log.d("Preferences", "Feedback was clicked")
                true // Return true if the click is handled.
            }
    }



}