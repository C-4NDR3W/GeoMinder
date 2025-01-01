package com.example.geominder

import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.Place
//i.google.anmport comdroid.libraries.places.api.model.AutocompletePrediction
//import com.google.android.libraries.places.api.model.FindAutocompletePredictionsRequest
//import com.google.android.libraries.places.api.net.PlacesClient
import android.Manifest
import kotlin.math.*

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geominder.models.PlaceRef
import com.example.geominder.models.Prediction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchBar: EditText
    private lateinit var predictionsList: RecyclerView
    private var googleMap: GoogleMap? = null


    //test

    //Item untuk layout place view
    private lateinit var placeViewLayout: LinearLayout
    private lateinit var addToNoteButton: Button
    private lateinit var placeViewImage: ImageView
    private lateinit var placeAddressField: TextView
    private lateinit var placeNameField: TextView
    private lateinit var placeRatingField: TextView
    private lateinit var openInGmapsButton: Button

    //biar bisa dipakai untuk location bias
    private lateinit var locationLightBox: ConstraintLayout
    private var currLatLng = LatLng(0.0, 0.0)

    private var results: MutableList<Prediction> = mutableListOf<Prediction>()
    private lateinit var placeSuggestionAdapter: PlaceAdapter

    // Set the adapter for the ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)


        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyBFXvpxCuHeq_1A8_iJxZazxyjvwCrjOaw")
        }

        // Initialize the requestPermissionLauncher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permission granted, proceed with location logic
                if (isLocationEnabled(requireContext())) {
                    getLastLocation()
                } else {
                    showLocationPrompt({ openLocationSettings() }, { getLastLocation() })
                }
            } else {
                // Permission denied, show rationale or navigate away
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }

                redirectToNote()
            }
        }

        // Continue with the rest of your onCreateView setup...
        mapView = view.findViewById(R.id.google_map)
        predictionsList = view.findViewById(R.id.placeSuggestionList)
        predictionsList.bringToFront()

        predictionsList.visibility = View.GONE
        placeSuggestionAdapter = PlaceAdapter(results, ::onItemClicked)

        predictionsList.adapter = placeSuggestionAdapter

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.isSmoothScrollbarEnabled = true
        predictionsList.layoutManager = layoutManager

        searchBar = view.findViewById(R.id.searchEditText)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        setEditTextListener(searchBar)

        setUpPlaceView(view)

        return view
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun openLocationSettings() {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun showLocationPrompt(positiveAction: () -> Unit, enableLocationAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Access Required")
            .setMessage(
                "This app requires location access to function properly. Please ensure location permissions are granted and location services are turned on." +
                        "Dismissing this dialogue will result in the user being ejected from the fragment."

            )
            .setPositiveButton("Enable Location") { _, _ ->
                openLocationSettings()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                redirectToNote()
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun setUpPlaceView(view: View) {
        placeViewLayout = view.findViewById(R.id.placeViewLayout)
        addToNoteButton = view.findViewById(R.id.addToNoteButton)
        placeViewImage = view.findViewById(R.id.placeViewImage)
        placeAddressField = view.findViewById(R.id.placeAddressField)
        placeNameField = view.findViewById(R.id.placeNameField)
        placeRatingField = view.findViewById(R.id.placeRatingField)
        openInGmapsButton = view.findViewById(R.id.openInMapsButton)

        placeViewLayout.visibility = View.GONE

    }





    private fun zoomToCoords(currentLatLng: LatLng) {
        googleMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            addMarker(MarkerOptions().position(currentLatLng).title("Your selected location"))
        }
    }

    private fun onItemClicked(currentPrediction: Prediction) {
        val id = currentPrediction.id
        val placesClient = Places.createClient(requireContext())
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.RATING,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        val request = FetchPlaceRequest.newInstance(id, placeFields)
        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            // Ensure that the place has the required details before using them
            val place = response.place
            val selected = PlaceRef(
                id = place.id ?: "",
                name = place.name ?: "",
                address = place.address ?: "",
                rating = place.rating,
                amountofRatings = place.userRatingsTotal ?: 0,
                latitude = place.latLng?.latitude ?: 0.0,
                longitude = place.latLng?.longitude ?: 0.0
            )

            Log.d("MapFragment", "Selected Place: $selected")
            bindViewDetails(selected)

            placeViewLayout.visibility = View.VISIBLE
            placeViewLayout.bringToFront()
            predictionsList.visibility = View.GONE

            val selectedLatLng = place.latLng!!
            zoomToCoords(selectedLatLng)

            // Now you can use `selected` for further processing
        }

//        animateViewHeight(placeViewLayout, 500)

    }


    private fun bindViewDetails(selectedPlace: PlaceRef) {

        placeNameField.text = selectedPlace.name
        placeAddressField.text = selectedPlace.address
        placeRatingField.text = when (selectedPlace.rating) {
            null -> "no rating"
            else -> "${selectedPlace.rating}/5 (${selectedPlace.amountofRatings})"
        }

        Log.d("placeID", selectedPlace.id)


        addToNoteButton.setOnClickListener {
            redirectToNote()
        }

        openInGmapsButton.setOnClickListener {
            redirectToGmaps(selectedPlace)
        }

    }


    private fun redirectToGmaps(selectedPlace: PlaceRef) {

        val placeId = selectedPlace.id
        val name = selectedPlace.name

        val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${name}&query_place_id=${placeId}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }


    private fun setEditTextListener(editText: EditText) {

        val placesClient = Places.createClient(requireContext())
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // No action needed before the text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
                placeViewLayout.visibility = View.GONE
//                results.clear()
                val newText = s.toString()
                getLastLocation()

                val bounds = RectangularBounds.newInstance(
                    LatLng(
                        currLatLng.latitude - 0.1,
                        currLatLng.longitude - 0.1
                    ),
                    LatLng(
                        currLatLng.latitude + 0.1,
                        currLatLng.longitude + 0.1
                    )
                )
                val autocompleteRequest = FindAutocompletePredictionsRequest.builder()
                    .setQuery(newText)
                    .setLocationBias(bounds)
                    .build()

                placesClient.findAutocompletePredictions(autocompleteRequest)
                    .addOnSuccessListener {
                        results.clear()
                        predictionsList.visibility = View.VISIBLE
                        predictionsList.bringToFront()
                        val numberOfPredictions = min(it.autocompletePredictions.size - 1, 7)
                        //take top 8 predictions
                        for (prediction in it.autocompletePredictions.slice(0..numberOfPredictions)) {
                            Log.d("MapFragment", "Nama tempat: ${prediction}, alamat: ")
                            val pred = Prediction(
                                prediction.placeId,
                                prediction.getPrimaryText(null).toString(),
                                prediction.getSecondaryText(null).toString(),
                                0.0,
                                0.0
                            )
                            results.add(pred)
                            Log.d("Pred", "Nama tempat: ${pred.name}, alamat: ${pred.address}")
                        }
                        placeSuggestionAdapter.notifyDataSetChanged()

                    }
                    .addOnFailureListener { error ->
                        Log.d("MapFragment", "Error: ${error.message}")
                    }

            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after the text changes
            }
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                predictionsList.visibility = View.VISIBLE
            } else {
                predictionsList.visibility = View.GONE
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
                redirectToNote()
            }
            .create().show()
    }


    fun getLastLocation() {
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

                    currLatLng = currentLatLng
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

        if (::placeNameField.isInitialized && placeNameField.text.isNotEmpty()) {
            bundle.putString("placeName", placeNameField.text.toString())

            val selectedPlace = googleMap?.cameraPosition?.target
            selectedPlace?.let {
                bundle.putDouble("latitude", it.latitude)
                bundle.putDouble("longitude", it.longitude)
            }
        }

        navController.navigate(R.id.action_mapFragment_to_noteCreatorFragment, bundle)
    }


    override fun onStart()
    {
        super.onStart()
        when {
            hasLocationPermission() -> {
                if (isLocationEnabled(requireContext())) {
                    getLastLocation()
                } else {
                    showLocationPrompt({ openLocationSettings() }, { getLastLocation() })
                }
            }
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {

                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> {
                requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

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

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)
        fab.visibility = View.GONE
        bottomNavigationView.visibility = View.GONE

        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab_add)
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)
        fab.visibility = View.VISIBLE
        bottomNavigationView.visibility = View.VISIBLE
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