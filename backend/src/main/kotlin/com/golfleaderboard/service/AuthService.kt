package com.golfleaderboard.service

import com.golfleaderboard.domain.User
import com.golfleaderboard.domain.UserRole
import com.golfleaderboard.repository.UserRepository
import com.golfleaderboard.security.JwtUtil
import com.golfleaderboard.web.dto.AuthResponse
import com.golfleaderboard.web.dto.LoginRequest
import com.golfleaderboard.web.dto.RegisterRequest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered: ${request.email}")
        }
        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            displayName = request.displayName ?: request.email.substringBefore('@'),
            role = UserRole.USER
        )
        val saved = userRepository.save(user)
        val token = jwtUtil.generateToken(saved.email, saved.id!!, saved.role.name)
        return AuthResponse(token = token, userId = saved.id!!, email = saved.email, displayName = saved.displayName, role = saved.role.name)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw BadCredentialsException("Invalid email or password")
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BadCredentialsException("Invalid email or password")
        }
        val token = jwtUtil.generateToken(user.email, user.id!!, user.role.name)
        return AuthResponse(token = token, userId = user.id!!, email = user.email, displayName = user.displayName, role = user.role.name)
    }
}
