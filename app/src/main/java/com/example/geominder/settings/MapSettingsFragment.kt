package com.example.geominder.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.geominder.MapFragment
import com.example.geominder.R


class MapSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.map_preferences, rootKey)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, _ ->
            notifyMapFragment()
        }
    }

    private fun notifyMapFragment() {
        val mapFragment = parentFragmentManager.findFragmentById(R.id.mapFragment) as? MapFragment
        mapFragment?.applyMapSettings()
    }
}
