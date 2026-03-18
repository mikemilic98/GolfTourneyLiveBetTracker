package com.golfleaderboard.web

import com.golfleaderboard.repository.UserRepository
import com.golfleaderboard.web.dto.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userRepository: UserRepository) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(): ResponseEntity<List<UserResponse>> {
        val users = userRepository.findAll().map { u ->
            UserResponse(id = u.id, email = u.email, displayName = u.displayName, role = u.role)
        }
        return ResponseEntity.ok(users)
    }
}
