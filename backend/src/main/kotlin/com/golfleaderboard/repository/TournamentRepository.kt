package com.golfleaderboard.repository

import com.golfleaderboard.domain.Tournament
import org.springframework.data.jpa.repository.JpaRepository

interface TournamentRepository : JpaRepository<Tournament, Long> {
    fun findByEspnEventId(espnEventId: String): Tournament?
    fun findAllByOrderByCreatedAtDesc(): List<Tournament>
    fun findTournamentsByEspnEventIdIsNotNull(): List<Tournament>
}
