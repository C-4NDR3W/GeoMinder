package com.example.geominder.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.geominder.R

class MapSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}