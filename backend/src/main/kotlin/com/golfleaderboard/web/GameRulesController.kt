package com.golfleaderboard.web

import com.golfleaderboard.service.GameRulesService
import com.golfleaderboard.web.dto.GameRulesCreateRequest
import com.golfleaderboard.web.dto.GameRulesUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rules")
class GameRulesController(private val gameRulesService: GameRulesService) {

    @GetMapping
    fun list() = ResponseEntity.ok(gameRulesService.findAll())

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<*> {
        val r = gameRulesService.findById(id)
        return if (r != null) ResponseEntity.ok(r) else ResponseEntity.notFound().build<Any>()
    }

    @GetMapping("/tournament/{tournamentId}")
    fun getByTournament(@PathVariable tournamentId: Long): ResponseEntity<*> {
        val r = gameRulesService.findByTournamentId(tournamentId)
        return if (r != null) ResponseEntity.ok(r) else ResponseEntity.notFound().build<Any>()
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: GameRulesCreateRequest): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(gameRulesService.create(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: GameRulesUpdateRequest): ResponseEntity<*> {
        return try {
            val r = gameRulesService.update(id, request)
            if (r != null) ResponseEntity.ok(r) else ResponseEntity.notFound().build<Any>()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<*> {
        return try {
            val deleted = gameRulesService.delete(id)
            if (deleted) ResponseEntity.noContent().build<Any>() else ResponseEntity.notFound().build<Any>()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
