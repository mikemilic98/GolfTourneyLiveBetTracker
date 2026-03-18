package com.golfleaderboard.service

import com.golfleaderboard.domain.User
import com.golfleaderboard.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService(private val userRepository: UserRepository) {

    fun getCurrentUser(): User? {
        val auth = SecurityContextHolder.getContext().authentication ?: return null
        if (!auth.isAuthenticated || auth.principal !is String) return null
        return userRepository.findByEmail(auth.principal as String)
    }

    fun getCurrentUserId(): Long? = getCurrentUser()?.id

    fun isAdmin(): Boolean =
        SecurityContextHolder.getContext().authentication?.authorities
            ?.any { it.authority == "ROLE_ADMIN" } == true
}
