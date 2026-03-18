package com.golfleaderboard.web.dto

import java.time.Instant

data class TournamentCreateRequest(
    @field:jakarta.validation.constraints.NotBlank val name: String,
    @field:jakarta.validation.constraints.NotBlank val espnUrl: String,
    val espnEventId: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val picksCutoff: Instant? = null
)

data class TournamentUpdateRequest(
    val name: String? = null,
    val espnUrl: String? = null,
    val espnEventId: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val picksCutoff: Instant? = null
)

data class TournamentResponse(
    val id: Long,
    val name: String,
    val espnUrl: String,
    val espnEventId: String?,
    val rosterCount: Int,
    val startTime: Instant?,
    val endTime: Instant?,
    val picksCutoff: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
