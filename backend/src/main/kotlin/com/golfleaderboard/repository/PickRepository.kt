package com.golfleaderboard.repository

import com.golfleaderboard.domain.Pick
import org.springframework.data.jpa.repository.JpaRepository

interface PickRepository : JpaRepository<Pick, Long> {
    fun findByUserIdAndTournamentId(userId: Long, tournamentId: Long): Pick?
    fun findByTournamentId(tournamentId: Long): List<Pick>
    fun findByUserId(userId: Long): List<Pick>
}
