package com.amirarahmah.toradi_user.data.model

data class OrderResponse(
    val code: Int,
    val message: String,
    val data: Order?
)