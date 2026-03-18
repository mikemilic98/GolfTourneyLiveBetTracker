package com.golfleaderboard.service

import com.golfleaderboard.domain.LeaderboardEntry
import com.golfleaderboard.domain.Tournament
import com.golfleaderboard.repository.LeaderboardEntryRepository
import com.golfleaderboard.repository.TournamentRepository
import com.golfleaderboard.scraper.EspnScraperService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LeaderboardService(
    private val leaderboardEntryRepository: LeaderboardEntryRepository,
    private val tournamentRepository: TournamentRepository,
    private val espnScraper: EspnScraperService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Fetches leaderboard from ESPN and replaces stored entries for the tournament.
     */
    @Transactional
    fun fetchAndStore(tournamentId: Long): List<LeaderboardEntry>? {
        val tournament = tournamentRepository.findById(tournamentId).orElse(null) ?: return null
        val raw = espnScraper.fetchLeaderboardFromApi(tournament.espnEventId)
        if (raw.isEmpty()) return emptyList()
        leaderboardEntryRepository.deleteByTournamentId(tournamentId)
        val now = Instant.now()
        val entries = raw.map { r ->
            LeaderboardEntry(
                tournament = tournament,
                playerName = r.playerName,
                position = r.position,
                toPar = r.toPar,
                thru = r.thru,
                fetchedAt = now
            )
        }
        return leaderboardEntryRepository.saveAll(entries)
    }

    fun getLatestEntries(tournamentId: Long): List<LeaderboardEntry> =
        leaderboardEntryRepository.findByTournamentIdOrderByPositionAsc(tournamentId)
}
