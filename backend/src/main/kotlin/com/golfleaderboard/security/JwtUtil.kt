package com.golfleaderboard.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-ms:86400000}") private val expirationMs: Long
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.encodeToByteArray())
    }

    fun generateToken(email: String, userId: Long, role: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)
        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun extractEmail(token: String): String? = extractClaims(token)?.subject

    fun extractUserId(token: String): Long? {
        val value = extractClaims(token)?.get("userId") ?: return null
        return (value as? Number)?.toLong()
    }

    fun extractRole(token: String): String? =
        extractClaims(token)?.get("role", String::class.java)

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractClaims(token) ?: return false
            !claims.expiration.before(Date())
        } catch (e: ExpiredJwtException) { false }
        catch (e: MalformedJwtException) { false }
        catch (e: SignatureException) { false }
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = extractClaims(token) ?: return false
            !claims.expiration.before(Date())
        } catch (e: ExpiredJwtException) { false }
        catch (e: MalformedJwtException) { false }
        catch (e: SignatureException) { false }
    }

    fun validateToken(token: String, email: String): Boolean {
        return try {
            val claims = extractClaims(token) ?: return false
            claims.subject == email && !claims.expiration.before(Date())
        } catch (e: ExpiredJwtException) { false }
        catch (e: MalformedJwtException) { false }
        catch (e: SignatureException) { false }
    }

    fun extractAllClaims(token: String): Claims = extractClaims(token)
        ?: throw io.jsonwebtoken.security.SignatureException("Invalid token")

    private fun extractClaims(token: String): Claims? = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
    } catch (e: Exception) { null }
}
