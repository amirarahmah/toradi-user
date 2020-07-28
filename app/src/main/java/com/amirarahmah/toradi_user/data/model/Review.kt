package com.amirarahmah.toradi_user.data.model

data class Review(
    val id: Int,
    val order_id: Int,
    val user_id: Int,
    val driver_id: Int,
    val rating: Int,
    val review: String,
    val type: Int,
    val created_at: String,
    val updated_at: String
)