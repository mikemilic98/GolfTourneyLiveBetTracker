package com.golfleaderboard.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var token: String? = request.getHeader("Authorization")?.takeIf { it.startsWith("Bearer ") }?.substring(7)
        if (token == null) token = request.getParameter("token")

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response)
            return
        }

        val claims = jwtUtil.extractAllClaims(token)
        val email = claims.subject
        val role = claims["role"]?.toString() ?: "USER"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

        val authToken = UsernamePasswordAuthenticationToken(email, null, authorities).apply {
            details = WebAuthenticationDetailsSource().buildDetails(request)
        }
        SecurityContextHolder.getContext().authentication = authToken

        filterChain.doFilter(request, response)
    }
}
