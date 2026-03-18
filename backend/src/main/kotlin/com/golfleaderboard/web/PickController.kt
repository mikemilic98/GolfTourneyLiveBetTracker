package com.golfleaderboard.web

import com.golfleaderboard.service.PickService
import com.golfleaderboard.web.dto.PickCreateForUserRequest
import com.golfleaderboard.web.dto.PickCreateRequest
import com.golfleaderboard.web.dto.PickUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/picks")
class PickController(private val pickService: PickService) {

    @GetMapping("/me")
    fun listMyPicks(@RequestParam(required = false) tournamentId: Long?): ResponseEntity<*> {
        val picks = pickService.findMyPicks(tournamentId)
        return ResponseEntity.ok(picks)
    }

    @GetMapping("/tournament/{tournamentId}")
    fun listByTournament(@PathVariable tournamentId: Long) =
        ResponseEntity.ok(pickService.findByTournamentId(tournamentId))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<*> {
        val p = pickService.findById(id)
        return if (p != null) ResponseEntity.ok(p) else ResponseEntity.notFound().build<Any>()
    }

    @PostMapping("/me")
    fun createMyPick(@Valid @RequestBody request: PickCreateForUserRequest): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(pickService.createForCurrentUser(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
        }
    }

    @PutMapping("/me/{id}")
    fun updateMyPick(@PathVariable id: Long, @Valid @RequestBody request: PickUpdateRequest): ResponseEntity<*> {
        return try {
            val p = pickService.updateForCurrentUser(id, request)
            if (p != null) ResponseEntity.ok(p) else ResponseEntity.notFound().build<Any>()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: PickCreateRequest): ResponseEntity<*> {
        return try {
            ResponseEntity.status(HttpStatus.CREATED).body(pickService.create(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: PickUpdateRequest): ResponseEntity<*> {
        return try {
            val p = pickService.update(id, request)
            if (p != null) ResponseEntity.ok(p) else ResponseEntity.notFound().build<Any>()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<*> {
        val deleted = pickService.delete(id)
        return if (deleted) ResponseEntity.noContent().build<Any>() else ResponseEntity.notFound().build<Any>()
    }
}
