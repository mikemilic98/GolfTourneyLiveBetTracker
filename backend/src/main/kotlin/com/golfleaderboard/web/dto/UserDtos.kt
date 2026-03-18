package com.golfleaderboard.web.dto

import com.golfleaderboard.domain.UserRole

data class UserResponse(
    val id: Long,
    val email: String,
    val displayName: String,
    val role: UserRole
)
