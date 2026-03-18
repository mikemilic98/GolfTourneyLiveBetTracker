package com.golfleaderboard.repository

import com.golfleaderboard.domain.LeaderboardEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface LeaderboardEntryRepository : JpaRepository<LeaderboardEntry, Long> {
    fun findByTournamentIdOrderByPositionAsc(tournamentId: Long): List<LeaderboardEntry>
    fun findByTournamentIdAndFetchedAtAfter(tournamentId: Long, fetchedAt: java.time.Instant): List<LeaderboardEntry>

    @Modifying
    @Query("DELETE FROM LeaderboardEntry le WHERE le.tournament.id = :tournamentId")
    fun deleteByTournamentId(tournamentId: Long)
}
