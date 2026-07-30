package com.example.solo_play_web_server.common.auth

import com.example.solo_play_web_server.auth.dto.TokenResponse
import com.example.solo_play_web_server.auth.enum.MemberRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

@Component
class JwtProvider(
    @Value("\${jwt.secret-key}")
    private val secretKey: String,

    @Value("\${jwt.access-token-expiry-ms}")
    private val accessTokenExpiryMs: Long,

    @Value("\${jwt.refresh-token-expiry-ms}")
    private val refreshTokenExpiryMs: Long,
) {

    private lateinit var key: Key

    @PostConstruct
    internal fun init() {
        // secretKey를 HMAC-SHA 키 객체로 변환
        key = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateTokens(userId: String, roles: Set<MemberRole>): TokenResponse {
        val now = Date()
        val accessToken = createAccessToken(now, userId, roles)
        val refreshToken = createRefreshToken(now, userId)
        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }

    private fun createAccessToken(now: Date, userId: String, roles: Set<MemberRole>): String {
        return Jwts.builder()
            .subject(userId)
            .claim("auth", roles.joinToString(",") { it.name }) // 역할을 claim에 추가
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenExpiryMs))
            .signWith(key)
            .compact()
    }

    private fun createRefreshToken(now: Date, userId: String): String {
        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(Date(now.time + refreshTokenExpiryMs))
            .signWith(key)
            .compact()
    }
}
