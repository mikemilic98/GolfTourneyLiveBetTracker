package com.golfleaderboard.web

import com.golfleaderboard.service.ScoringService
import com.golfleaderboard.service.SseBroadcastService
import com.golfleaderboard.web.dto.LiveLeaderboardDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/live")
class LiveLeaderboardController(
    private val scoringService: ScoringService,
    private val sseBroadcastService: SseBroadcastService
) {

    @GetMapping("/{tournamentId}/scores")
    fun getScores(@PathVariable tournamentId: Long): ResponseEntity<*> {
        val scores = scoringService.getLeaderboard(tournamentId)
        val dto = LiveLeaderboardDto(
            tournamentId = tournamentId,
            rows = scores.map { LiveLeaderboardDto.LeaderboardRow(it.user.displayName, it.totalScore, it.rank) }
        )
        return ResponseEntity.ok(dto)
    }

    @GetMapping(value = ["/{tournamentId}/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@PathVariable tournamentId: Long): SseEmitter {
        return sseBroadcastService.subscribe(tournamentId)
    }
}
