package com.golfleaderboard.repository

import com.golfleaderboard.domain.UserScore
import org.springframework.data.jpa.repository.JpaRepository

interface UserScoreRepository : JpaRepository<UserScore, Long> {
    fun findByTournamentIdOrderByTotalScoreAsc(tournamentId: Long): List<UserScore>
    fun findByUserIdAndTournamentId(userId: Long, tournamentId: Long): UserScore?
}
