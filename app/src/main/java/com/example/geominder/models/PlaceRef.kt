package com.example.geominder.models

import com.google.android.libraries.places.api.model.OpeningHours

data class PlaceRef(
    val id: String,
    val name: String,
    val address: String,
    val rating: Double?,
    val amountofRatings: Int,
    val latitude: Double,
    val longitude: Double,
)
