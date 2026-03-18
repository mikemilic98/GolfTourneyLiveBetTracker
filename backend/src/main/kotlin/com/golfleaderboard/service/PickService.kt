package com.golfleaderboard.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.golfleaderboard.domain.Pick
import com.golfleaderboard.domain.Tournament
import com.golfleaderboard.domain.User
import com.golfleaderboard.repository.PickRepository
import com.golfleaderboard.repository.TournamentRepository
import com.golfleaderboard.repository.UserRepository
import com.golfleaderboard.util.PlayerNameNormalizer
import com.golfleaderboard.web.dto.PickCreateForUserRequest
import com.golfleaderboard.web.dto.PickCreateRequest
import com.golfleaderboard.web.dto.PickResponse
import com.golfleaderboard.web.dto.PickUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PickService(
    private val pickRepository: PickRepository,
    private val userRepository: UserRepository,
    private val tournamentRepository: TournamentRepository,
    private val currentUserService: CurrentUserService,
    private val objectMapper: ObjectMapper
) {

    fun findAll(): List<PickResponse> =
        pickRepository.findAll().map { toResponse(it) }

    fun findById(id: Long): PickResponse? =
        pickRepository.findById(id).map { toResponse(it) }.orElse(null)

    fun findByTournamentId(tournamentId: Long): List<PickResponse> =
        pickRepository.findByTournamentId(tournamentId).map { toResponse(it) }

    fun findByUserId(userId: Long): List<PickResponse> =
        pickRepository.findByUserId(userId).map { toResponse(it) }

    fun findMyPicks(tournamentId: Long?): List<PickResponse> {
        val userId = currentUserService.getCurrentUserId() ?: return emptyList()
        val picks = pickRepository.findByUserId(userId)
        return if (tournamentId != null) {
            picks.filter { it.tournament.id == tournamentId }.map { toResponse(it) }
        } else {
            picks.map { toResponse(it) }
        }
    }

    /** User creates pick for themselves. Enforces cutoff and roster validation. */
    @Transactional
    fun createForCurrentUser(request: PickCreateForUserRequest): PickResponse {
        val user = currentUserService.getCurrentUser()
            ?: throw IllegalArgumentException("Not authenticated")
        if (currentUserService.isAdmin()) {
            return create(PickCreateRequest(userId = user.id, tournamentId = request.tournamentId, playerNames = request.playerNames))
        }
        val tournament = tournamentRepository.findById(request.tournamentId)
            .orElseThrow { IllegalArgumentException("Tournament not found: ${request.tournamentId}") }
        checkBeforeCutoff(tournament, "create")
        validatePlayersAgainstRoster(tournament, request.playerNames)
        val existing = pickRepository.findByUserIdAndTournamentId(user.id, request.tournamentId)
        if (existing != null) {
            throw IllegalArgumentException("Pick already exists for this tournament")
        }
        val pick = Pick(user = user, tournament = tournament, playerNames = request.playerNames.toMutableList())
        return toResponse(pickRepository.save(pick))
    }

    /** User updates their own pick. Enforces cutoff for non-admins. */
    @Transactional
    fun updateForCurrentUser(id: Long, request: PickUpdateRequest): PickResponse? {
        val pick = pickRepository.findById(id).orElse(null) ?: return null
        val user = currentUserService.getCurrentUser() ?: return null
        if (pick.user.id != user.id && !currentUserService.isAdmin()) {
            throw IllegalArgumentException("Cannot update another user's pick")
        }
        if (!currentUserService.isAdmin()) {
            checkBeforeCutoff(pick.tournament, "update")
            validatePlayersAgainstRoster(pick.tournament, request.playerNames)
        }
        pick.playerNames.clear()
        pick.playerNames.addAll(request.playerNames)
        pick.updatedAt = Instant.now()
        return toResponse(pickRepository.save(pick))
    }

    private fun checkBeforeCutoff(tournament: Tournament, action: String) {
        val cutoff = tournament.picksCutoff ?: return
        if (Instant.now().isAfter(cutoff)) {
            throw IllegalStateException("Picks cutoff has passed. $action is no longer allowed.")
        }
    }

    private fun validatePlayersAgainstRoster(tournament: Tournament, playerNames: List<String>) {
        val roster = parseRoster(tournament.rosterSnapshot) ?: return
        val rosterSet = roster.map { normalizeName(it) }.toSet()
        val invalid = playerNames.filter { normalizeName(it) !in rosterSet }
        if (invalid.isNotEmpty()) {
            throw IllegalArgumentException("Players not in roster: ${invalid.joinToString(", ")}")
        }
    }

    private fun normalizeName(s: String) = PlayerNameNormalizer.normalize(s)

    private fun parseRoster(json: String?): List<String>? {
        if (json == null) return null
        return try {
            @Suppress("UNCHECKED_CAST")
            objectMapper.readValue(json, List::class.java) as List<String>
        } catch (_: Exception) {
            null
        }
    }

    @Transactional
    fun create(request: PickCreateRequest): PickResponse {
        val user = userRepository.findById(request.userId)
            .orElseThrow { IllegalArgumentException("User not found: ${request.userId}") }
        val tournament = tournamentRepository.findById(request.tournamentId)
            .orElseThrow { IllegalArgumentException("Tournament not found: ${request.tournamentId}") }
        val existing = pickRepository.findByUserIdAndTournamentId(request.userId, request.tournamentId)
        if (existing != null) {
            throw IllegalArgumentException("Pick already exists for user ${request.userId} in tournament ${request.tournamentId}")
        }
        val pick = Pick(
            user = user,
            tournament = tournament,
            playerNames = request.playerNames.toMutableList()
        )
        return toResponse(pickRepository.save(pick))
    }

    /** Admin update - no cutoff/roster enforcement */
    @Transactional
    fun update(id: Long, request: PickUpdateRequest): PickResponse? {
        val pick = pickRepository.findById(id).orElse(null) ?: return null
        pick.playerNames.clear()
        pick.playerNames.addAll(request.playerNames)
        pick.updatedAt = Instant.now()
        return toResponse(pickRepository.save(pick))
    }

    @Transactional
    fun delete(id: Long): Boolean {
        if (!pickRepository.existsById(id)) return false
        pickRepository.deleteById(id)
        return true
    }

    private fun toResponse(p: Pick) = PickResponse(
        id = p.id,
        userId = p.user.id,
        userDisplayName = p.user.displayName,
        tournamentId = p.tournament.id,
        tournamentName = p.tournament.name,
        playerNames = p.playerNames.toList(),
        createdAt = p.createdAt,
        updatedAt = p.updatedAt
    )
}
