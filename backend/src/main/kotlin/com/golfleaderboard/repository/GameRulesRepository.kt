package com.golfleaderboard.repository

import com.golfleaderboard.domain.GameRules
import org.springframework.data.jpa.repository.JpaRepository

interface GameRulesRepository : JpaRepository<GameRules, Long> {
    fun findByTournament_Id(tournamentId: Long): GameRules?
}
