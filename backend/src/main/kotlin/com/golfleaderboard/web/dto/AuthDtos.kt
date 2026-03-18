package com.golfleaderboard.web.dto

import com.golfleaderboard.domain.UserRole

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val displayName: String,
    val role: UserRole
)
