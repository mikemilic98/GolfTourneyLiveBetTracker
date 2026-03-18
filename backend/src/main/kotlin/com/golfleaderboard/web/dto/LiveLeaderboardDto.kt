package com.golfleaderboard.web.dto

import java.time.Instant

data class LiveLeaderboardDto(
    val tournamentId: Long,
    val rows: List<LeaderboardRow>,
    val computedAt: Instant = Instant.now()
) {
    data class LeaderboardRow(
        val displayName: String,
        val totalScore: Int,
        val rank: Int?
    )
}
