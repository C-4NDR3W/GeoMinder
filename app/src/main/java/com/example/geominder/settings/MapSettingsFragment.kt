package com.example.geominder.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.example.geominder.MapFragment
import com.example.geominder.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap


class MapSettingsFragment : PreferenceFragmentCompat() {
    private lateinit var googleMap: GoogleMap
    private lateinit var scaleBar: GoogleMap

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
