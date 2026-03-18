package com.golfleaderboard.web

import com.golfleaderboard.service.TournamentService
import com.golfleaderboard.web.dto.TournamentCreateRequest
import com.golfleaderboard.web.dto.TournamentUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tournaments")
class TournamentController(private val tournamentService: TournamentService) {

    @GetMapping
    fun list() = ResponseEntity.ok(tournamentService.findAll())

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<*> {
        val t = tournamentService.findById(id)
        return if (t != null) ResponseEntity.ok(t) else ResponseEntity.notFound().build<Any>()
    }

    @GetMapping("/{id}/roster")
    fun getRoster(@PathVariable id: Long): ResponseEntity<*> {
        val roster = tournamentService.getRoster(id)
        return if (roster != null) ResponseEntity.ok(roster) else ResponseEntity.notFound().build<Any>()
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: TournamentCreateRequest) =
        ResponseEntity.status(HttpStatus.CREATED).body(tournamentService.create(request))

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: TournamentUpdateRequest): ResponseEntity<*> {
        val t = tournamentService.update(id, request)
        return if (t != null) ResponseEntity.ok(t) else ResponseEntity.notFound().build<Any>()
    }

    @PostMapping("/{id}/ingest-roster")
    @PreAuthorize("hasRole('ADMIN')")
    fun ingestRoster(@PathVariable id: Long): ResponseEntity<*> {
        val roster = tournamentService.ingestRoster(id)
        return if (roster != null) ResponseEntity.ok(roster) else ResponseEntity.notFound().build<Any>()
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<*> {
        val deleted = tournamentService.delete(id)
        return if (deleted) ResponseEntity.noContent().build<Any>() else ResponseEntity.notFound().build<Any>()
    }
}
