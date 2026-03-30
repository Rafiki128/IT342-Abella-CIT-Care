package com.abella.cit_care

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String
)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String?, val message: String?)