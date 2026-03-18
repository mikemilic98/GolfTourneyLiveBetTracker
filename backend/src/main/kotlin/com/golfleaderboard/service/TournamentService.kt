package com.golfleaderboard.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.golfleaderboard.domain.Tournament
import com.golfleaderboard.repository.TournamentRepository
import com.golfleaderboard.scraper.EspnScraperService
import com.golfleaderboard.scraper.RawLeaderboardEntry
import com.golfleaderboard.web.dto.TournamentCreateRequest
import com.golfleaderboard.web.dto.TournamentResponse
import com.golfleaderboard.web.dto.TournamentUpdateRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TournamentService(
    private val tournamentRepository: TournamentRepository,
    private val espnScraper: EspnScraperService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun findAll(): List<TournamentResponse> =
        tournamentRepository.findAllByOrderByCreatedAtDesc().map { toResponse(it) }

    fun findById(id: Long): TournamentResponse? =
        tournamentRepository.findById(id).map { toResponse(it) }.orElse(null)

    @Transactional
    fun create(request: TournamentCreateRequest): TournamentResponse {
        val espnEventId = request.espnEventId ?: extractEspnEventId(request.espnUrl)
        val tournament = Tournament(
            name = request.name,
            espnUrl = request.espnUrl,
            espnEventId = espnEventId,
            startTime = request.startTime,
            endTime = request.endTime,
            picksCutoff = request.picksCutoff
        )
        return toResponse(tournamentRepository.save(tournament))
    }

    @Transactional
    fun update(id: Long, request: TournamentUpdateRequest): TournamentResponse? {
        val existing = tournamentRepository.findById(id).orElse(null) ?: return null
        request.name?.let { existing.name = it }
        request.espnUrl?.let { existing.espnUrl = it; existing.espnEventId = extractEspnEventId(it) }
        request.startTime?.let { existing.startTime = it }
        request.endTime?.let { existing.endTime = it }
        request.picksCutoff?.let { existing.picksCutoff = it }
        existing.updatedAt = Instant.now()
        return toResponse(tournamentRepository.save(existing))
    }

    @Transactional
    fun delete(id: Long): Boolean {
        if (!tournamentRepository.existsById(id)) return false
        tournamentRepository.deleteById(id)
        return true
    }

    /**
     * Ingests roster from ESPN. Uses HTML Tournament Field for player names.
     * Also tries leaderboard API if event has competitors (pre-tournament list).
     */
    @Transactional
    fun ingestRoster(tournamentId: Long): List<String>? {
        val tournament = tournamentRepository.findById(tournamentId).orElse(null) ?: return null
        val roster = mutableSetOf<String>()

        // Try API first - competitors list when event exists
        val apiEntries = espnScraper.fetchLeaderboardFromApi(tournament.espnEventId)
        apiEntries.forEach { roster.add(it.playerName) }

        // Always try HTML for Tournament Field (more complete before event)
        if (roster.isEmpty() || apiEntries.size < 50) {
            val htmlRoster = espnScraper.fetchRosterFromHtml(tournament.espnUrl)
            roster.addAll(htmlRoster)
        }

        val sorted = roster.sorted()
        tournament.rosterSnapshot = objectMapper.writeValueAsString(sorted)
        tournament.updatedAt = Instant.now()
        tournamentRepository.save(tournament)
        log.info("Ingested roster for tournament ${tournament.id}: ${sorted.size} players")
        return sorted
    }

    fun getRoster(tournamentId: Long): List<String>? {
        val tournament = tournamentRepository.findById(tournamentId).orElse(null) ?: return null
        val json = tournament.rosterSnapshot ?: return emptyList()
        return try {
            @Suppress("UNCHECKED_CAST")
            objectMapper.readValue(json, List::class.java) as List<String>
        } catch (e: Exception) {
            log.warn("Failed to parse roster JSON: ${e.message}")
            emptyList()
        }
    }

    private fun extractEspnEventId(url: String): String? {
        val regex = Regex("""tournamentId=(\d+)""")
        return regex.find(url)?.groupValues?.get(1)
    }

    private fun toResponse(t: Tournament) = TournamentResponse(
        id = t.id,
        name = t.name,
        espnUrl = t.espnUrl,
        espnEventId = t.espnEventId,
        rosterCount = parseRosterCount(t.rosterSnapshot),
        startTime = t.startTime,
        endTime = t.endTime,
        picksCutoff = t.picksCutoff,
        createdAt = t.createdAt,
        updatedAt = t.updatedAt
    )

    private fun parseRosterCount(json: String?): Int {
        if (json == null) return 0
        return try {
            val list = objectMapper.readValue(json, List::class.java)
            list.size
        } catch (_: Exception) {
            0
        }
    }
}
