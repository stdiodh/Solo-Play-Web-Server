package com.example.solo_play_web_server.common.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

@Component
class TokenProvider(
    @Value("\${jwt.secret-key}")
    private val secretKey: String,
) {
    private lateinit var key: SecretKey

    @PostConstruct
    internal fun init() {
        key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    private fun parseClaims(accessToken: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(accessToken)
            .payload
    }

    fun getAuthentication(accessToken: String): Authentication {
        val claims = parseClaims(accessToken)

        val authorities: Collection<GrantedAuthority> = (claims["auth"] as? String)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { SimpleGrantedAuthority("ROLE_$it") }
            ?: emptyList()

        val principal = User(claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, accessToken, authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims = parseClaims(token)
            val authorities = claims["auth"] as? String
            val hasAuthority = authorities
                ?.split(",")
                ?.any { it.isNotBlank() }
                ?: false
            return !claims.subject.isNullOrBlank() && hasAuthority
        } catch (e: Exception) {
            return false
        }
    }
}
