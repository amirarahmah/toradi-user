package com.amirarahmah.toradi_user.data.model

data class Order(
    val id: Int,
    val user_id: Int,
    val user_name: String,
    val driver_id: Int?,
    val driver_name: String?,
    val license_plat: String?,
    val status: Int,
    var status_text: String = "",
    val pickup_lat: Double,
    val pickup_lng: Double,
    val pickup_address: String,
    val destination_lat: Double,
    val destination_lng: Double,
    val destination_address: String,
    val price: Int,
    val distance: Double,
    val note: String,
    val created_at: String
)