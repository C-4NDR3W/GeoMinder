package com.example.geominder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var toggleButton: ToggleButton
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        toggleButton = view.findViewById(R.id.toggleButton)
        mapView = view.findViewById(R.id.google_map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toggleState = arguments?.getBoolean("toggleState", false) ?: false
        toggleButton.isChecked = toggleState

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                redirectToNote()
            }
        }
    }

    private fun redirectToNote() {
        val navController = findNavController()

        val bundle = Bundle()
        bundle.putBoolean("toggleState", toggleButton.isChecked)

        navController.navigate(R.id.action_mapFragment_to_navigation_home, bundle)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}