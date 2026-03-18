package com.golfleaderboard.web.dto

import java.time.Instant

data class GameRulesCreateRequest(
    val tournamentId: Long,
    @field:jakarta.validation.constraints.Min(1) val numPicks: Int = 5,
    @field:jakarta.validation.constraints.Min(0) val numDropped: Int = 1,
    val tieRule: String = "LOWEST_SUM",
    @field:jakarta.validation.constraints.Min(1) val wdPenaltyPosition: Int = 200
)

data class GameRulesUpdateRequest(
    val numPicks: Int? = null,
    val numDropped: Int? = null,
    val tieRule: String? = null,
    val wdPenaltyPosition: Int? = null,
    val lock: Boolean = false
)

data class GameRulesResponse(
    val id: Long,
    val tournamentId: Long,
    val tournamentName: String,
    val numPicks: Int,
    val numDropped: Int,
    val tieRule: String,
    val wdPenaltyPosition: Int,
    val lockedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
