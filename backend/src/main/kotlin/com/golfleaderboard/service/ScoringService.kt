package com.golfleaderboard.service

import com.golfleaderboard.domain.GameRules
import com.golfleaderboard.domain.LeaderboardEntry
import com.golfleaderboard.domain.Pick
import com.golfleaderboard.domain.UserScore
import com.golfleaderboard.repository.GameRulesRepository
import com.golfleaderboard.repository.LeaderboardEntryRepository
import com.golfleaderboard.repository.PickRepository
import com.golfleaderboard.repository.UserScoreRepository
import com.golfleaderboard.util.PlayerNameNormalizer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ScoringService(
    private val pickRepository: PickRepository,
    private val leaderboardEntryRepository: LeaderboardEntryRepository,
    private val gameRulesRepository: GameRulesRepository,
    private val userScoreRepository: UserScoreRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Recomputes all user scores for a tournament from picks + leaderboard + rules.
     */
    @Transactional
    fun recomputeScores(tournamentId: Long): List<UserScore> {
        val rules = gameRulesRepository.findByTournament_Id(tournamentId)
            ?: run {
                log.debug("No rules for tournament $tournamentId")
                return emptyList()
            }
        val entries = leaderboardEntryRepository.findByTournamentIdOrderByPositionAsc(tournamentId)
        if (entries.isEmpty()) return emptyList()
        val posMap = entries.associate { normalizeName(it.playerName) to it.position }
        val wdPenalty = rules.wdPenaltyPosition
        val picks = pickRepository.findByTournamentId(tournamentId)
        val scores = picks.map { pick ->
            val positions = pick.playerNames.map { name ->
                posMap[normalizeName(name)] ?: wdPenalty
            }.sortedDescending()
            val dropped = positions.take(rules.numDropped)
            val summed = positions.drop(rules.numDropped).sum()
            UserScore(
                user = pick.user,
                tournament = pick.tournament,
                totalScore = summed,
                computedAt = Instant.now()
            )
        }
        userScoreRepository.findByTournamentIdOrderByTotalScoreAsc(tournamentId).forEach {
            userScoreRepository.delete(it)
        }
        val saved = userScoreRepository.saveAll(scores)
        var rank = 1
        saved.sortedBy { it.totalScore }.forEach {
            it.rank = rank++
            userScoreRepository.save(it)
        }
        return userScoreRepository.findByTournamentIdOrderByTotalScoreAsc(tournamentId)
    }

    fun getLeaderboard(tournamentId: Long): List<UserScore> =
        userScoreRepository.findByTournamentIdOrderByTotalScoreAsc(tournamentId)

    private fun normalizeName(s: String) = PlayerNameNormalizer.normalize(s)
}
