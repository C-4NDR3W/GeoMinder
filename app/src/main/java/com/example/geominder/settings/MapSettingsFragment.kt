package com.example.geominder.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.example.geominder.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap


class MapSettingsFragment : PreferenceFragmentCompat() {
    private lateinit var googleMap: GoogleMap
    private lateinit var scaleBar: GoogleMap

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.map_preferences, rootKey)

        val mapTypePreference = findPreference<ListPreference>("map_type")
        mapTypePreference?.setOnPreferenceChangeListener { _, newValue ->
            updateMapType(newValue as String)
            true
        }

        val trafficLayerPreference = findPreference<SwitchPreferenceCompat>("traffic_layer")
        trafficLayerPreference?.setOnPreferenceChangeListener { _, newValue ->
            updateTrafficLayer(newValue as Boolean)
            true
        }

        val terrainLayerPreference = findPreference<SwitchPreferenceCompat>("terrain_layer")
        terrainLayerPreference?.setOnPreferenceChangeListener { _, newValue ->
            updateTerrainLayer(newValue as Boolean)
            true
        }

        val defaultZoomPreference = findPreference<SeekBarPreference>("default_zoom")
        defaultZoomPreference?.setOnPreferenceChangeListener { _, newValue ->
            updateDefaultZoom(newValue as Int)
            true
        }

        val scaleBarPreference = findPreference<SwitchPreferenceCompat>("show_scale_bar")
        scaleBarPreference?.setOnPreferenceChangeListener { _, newValue ->
            updateScaleBarVisibility(newValue as Boolean)
            true
        }
    }

    private fun updateMapType(mapType: String) {
        val googleMapType = when (mapType) {
            "normal" -> GoogleMap.MAP_TYPE_NORMAL
            "satellite" -> GoogleMap.MAP_TYPE_SATELLITE
            "terrain" -> GoogleMap.MAP_TYPE_TERRAIN
            "hybrid" -> GoogleMap.MAP_TYPE_HYBRID
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
        googleMap.mapType = googleMapType
        println("Updated map type to: $mapType")
    }

    private fun updateTrafficLayer(enabled: Boolean) {
        googleMap.isTrafficEnabled = enabled
        println("Traffic layer is now ${if (enabled) "enabled" else "disabled"}")
    }

    private fun updateTerrainLayer(enabled: Boolean) {
        if (enabled) {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        } else {
            // Reset to a default map type (e.g., Normal)
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun updateDefaultZoom(zoomLevel: Int) {
        val currentLocation = googleMap.cameraPosition.target
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel.toFloat()))

    }

    private fun updateScaleBarVisibility(visible: Boolean) {
//        if (visible) {
//            scaleBar.show()
//        } else {
//            scaleBar.hide()
//        }
    }
}