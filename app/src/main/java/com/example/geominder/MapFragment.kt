package com.example.geominder

import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.Place
//i.google.anmport comdroid.libraries.places.api.model.AutocompletePrediction
//import com.google.android.libraries.places.api.model.FindAutocompletePredictionsRequest
//import com.google.android.libraries.places.api.net.PlacesClient
import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.widget.AutocompleteSupportFragment

private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var toggleButton: SwitchCompat
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        toggleButton = view.findViewById(R.id.toggleButton)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLastLocation()
                } else {
                    showPermissionRationale {
                        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }
            }
    }


    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }



    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        val priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        val cancellationTokenSource = CancellationTokenSource()

        //dapatkan lokasi terbaru dari fusedLocationClient

        fusedLocationClient.getCurrentLocation(priority, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                Log.e("MapFragment", "Location: $location")
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.apply {
                        moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                    }
                }
                //notif kalo lokasi null
                else {
                    Toast.makeText(
                        requireContext(),
                        "Unable to get current location.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapFragment", "Failed to get last location: ${e.message}")
                Toast.makeText(requireContext(), "Failed to get location.", Toast.LENGTH_SHORT)
                    .show()
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

        applyMapSettings()
//        Toast.makeText(requireContext(), "Maps Accessed", Toast.LENGTH_SHORT).show()
        when {
            hasLocationPermission() -> getLastLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
        }

        applyMapSettings()
    }

    fun applyMapSettings() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Map type
        val mapType = sharedPreferences.getString("map_type", "normal") ?: "normal"
        val googleMapType = when (mapType) {
            "normal" -> GoogleMap.MAP_TYPE_NORMAL
            "satellite" -> GoogleMap.MAP_TYPE_SATELLITE
            "terrain" -> GoogleMap.MAP_TYPE_TERRAIN
            "hybrid" -> GoogleMap.MAP_TYPE_HYBRID
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
        googleMap?.mapType = googleMapType

        // Traffic layer
        val trafficEnabled = sharedPreferences.getBoolean("traffic_layer", false)
        googleMap?.isTrafficEnabled = trafficEnabled

        // Default zoom (optional)
        val zoomLevel = sharedPreferences.getInt("default_zoom", 15)
        googleMap?.moveCamera(
            CameraUpdateFactory.zoomTo(zoomLevel.toFloat())
        )

        val showZoomControls = sharedPreferences.getBoolean("show_zoom_controls", true)

        Log.d(requireContext().toString(), "Map settings applied!")
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