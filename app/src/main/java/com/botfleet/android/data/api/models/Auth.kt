package com.botfleet.android.data.api.models

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val id: Int,
    val username: String,
    val email: String,
    val isAdmin: Int,
    val token: String
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val isAdmin: Int
)
