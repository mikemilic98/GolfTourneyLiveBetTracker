package com.golfleaderboard.service

import com.golfleaderboard.domain.UserScore
import com.golfleaderboard.web.dto.LiveLeaderboardDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Service
class SseBroadcastService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val emitters = ConcurrentHashMap<Long, MutableSet<SseEmitter>>()

    fun subscribe(tournamentId: Long): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters.getOrPut(tournamentId) { ConcurrentHashMap.newKeySet() }.add(emitter)
        emitter.onCompletion { emitters[tournamentId]?.remove(emitter) }
        emitter.onTimeout { emitters[tournamentId]?.remove(emitter) }
        return emitter
    }

    fun broadcast(tournamentId: Long, scores: List<UserScore>) {
        val set = emitters[tournamentId] ?: return
        val dto = LiveLeaderboardDto(tournamentId, scores.map { s ->
            LiveLeaderboardDto.LeaderboardRow(s.user.displayName, s.totalScore, s.rank)
        })
        val dead = mutableListOf<SseEmitter>()
        set.forEach { emitter ->
            try {
                emitter.send(SseEmitter.event().data(dto))
            } catch (e: Exception) {
                dead.add(emitter)
            }
        }
        dead.forEach { set.remove(it) }
    }
}
