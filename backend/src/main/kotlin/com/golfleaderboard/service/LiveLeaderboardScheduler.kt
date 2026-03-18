package com.golfleaderboard.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class LiveLeaderboardScheduler(
    private val leaderboardService: LeaderboardService,
    private val scoringService: ScoringService,
    private val tournamentRepository: com.golfleaderboard.repository.TournamentRepository,
    private val sseBroadcastService: SseBroadcastService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 5000)
    fun fetchAndRecompute() {
        val tournaments = tournamentRepository.findTournamentsByEspnEventIdIsNotNull()
        for (t in tournaments) {
            try {
                val entries = leaderboardService.fetchAndStore(t.id)
                if (!entries.isNullOrEmpty()) {
                    val scores = scoringService.recomputeScores(t.id)
                    sseBroadcastService.broadcast(t.id, scores)
                }
            } catch (e: Exception) {
                log.warn("Scheduler error for tournament ${t.id}: ${e.message}")
            }
        }
    }
}
