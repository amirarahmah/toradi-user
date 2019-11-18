package com.amirarahmah.toradi_user.data.model

data class Geocoding(
    val status: String,
    val results: List<GeocodeResult>
)