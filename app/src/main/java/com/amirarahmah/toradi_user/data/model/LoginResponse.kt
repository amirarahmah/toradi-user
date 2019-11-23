package com.amirarahmah.toradi_user.data.model

data class LoginResponse(
    val code: Int,
    val message: String,
    val token: String,
    val data: User
)