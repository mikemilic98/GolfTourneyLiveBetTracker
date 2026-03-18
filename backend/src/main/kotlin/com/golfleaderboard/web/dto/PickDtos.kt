package com.golfleaderboard.web.dto

import java.time.Instant

data class PickCreateRequest(
    val userId: Long,
    val tournamentId: Long,
    @field:jakarta.validation.constraints.NotEmpty val playerNames: List<String>
)

/** For users creating their own pick (no userId - uses current user) */
data class PickCreateForUserRequest(
    val tournamentId: Long,
    @field:jakarta.validation.constraints.NotEmpty val playerNames: List<String>
)

data class PickUpdateRequest(
    val playerNames: List<String>
)

data class PickResponse(
    val id: Long,
    val userId: Long,
    val userDisplayName: String,
    val tournamentId: Long,
    val tournamentName: String,
    val playerNames: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant
)
