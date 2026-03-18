package com.golfleaderboard.service

import com.golfleaderboard.domain.GameRules
import com.golfleaderboard.domain.Tournament
import com.golfleaderboard.repository.GameRulesRepository
import com.golfleaderboard.repository.TournamentRepository
import com.golfleaderboard.web.dto.GameRulesCreateRequest
import com.golfleaderboard.web.dto.GameRulesResponse
import com.golfleaderboard.web.dto.GameRulesUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class GameRulesService(
    private val gameRulesRepository: GameRulesRepository,
    private val tournamentRepository: TournamentRepository
) {

    fun findAll(): List<GameRulesResponse> =
        gameRulesRepository.findAll().map { toResponse(it) }

    fun findById(id: Long): GameRulesResponse? =
        gameRulesRepository.findById(id).map { toResponse(it) }.orElse(null)

    fun findByTournamentId(tournamentId: Long): GameRulesResponse? =
        gameRulesRepository.findByTournament_Id(tournamentId)?.let { toResponse(it) }

    @Transactional
    fun create(request: GameRulesCreateRequest): GameRulesResponse {
        val tournament = tournamentRepository.findById(request.tournamentId)
            .orElseThrow { IllegalArgumentException("Tournament not found: ${request.tournamentId}") }
        if (gameRulesRepository.findByTournament_Id(request.tournamentId) != null) {
            throw IllegalArgumentException("Game rules already exist for tournament ${request.tournamentId}")
        }
        val rules = GameRules(
            tournament = tournament,
            numPicks = request.numPicks,
            numDropped = request.numDropped,
            tieRule = request.tieRule,
            wdPenaltyPosition = request.wdPenaltyPosition
        )
        return toResponse(gameRulesRepository.save(rules))
    }

    @Transactional
    fun update(id: Long, request: GameRulesUpdateRequest): GameRulesResponse? {
        val existing = gameRulesRepository.findById(id).orElse(null) ?: return null
        val rules = existing
        if (rules.lockedAt != null) {
            throw IllegalStateException("Rules are locked and cannot be updated")
        }
        request.numPicks?.let { rules.numPicks = it }
        request.numDropped?.let { rules.numDropped = it }
        request.tieRule?.let { rules.tieRule = it }
        request.wdPenaltyPosition?.let { rules.wdPenaltyPosition = it }
        if (request.lock) {
            rules.lockedAt = Instant.now()
        }
        rules.updatedAt = Instant.now()
        return toResponse(gameRulesRepository.save(rules))
    }

    @Transactional
    fun delete(id: Long): Boolean {
        if (!gameRulesRepository.existsById(id)) return false
        val rules = gameRulesRepository.findById(id).orElse(null)
        if (rules?.lockedAt != null) {
            throw IllegalStateException("Cannot delete locked rules")
        }
        gameRulesRepository.deleteById(id)
        return true
    }

    private fun toResponse(r: GameRules) = GameRulesResponse(
        id = r.id,
        tournamentId = r.tournament.id,
        tournamentName = r.tournament.name,
        numPicks = r.numPicks,
        numDropped = r.numDropped,
        tieRule = r.tieRule,
        wdPenaltyPosition = r.wdPenaltyPosition,
        lockedAt = r.lockedAt,
        createdAt = r.createdAt,
        updatedAt = r.updatedAt
    )
}
