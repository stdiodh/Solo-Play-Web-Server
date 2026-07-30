package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.entity.RefreshToken
import com.example.solo_play_web_server.common.repository.AbstractRedisRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRepository(
    @Qualifier("refreshTokenRedisTemplate")
    override val redisTemplate: ReactiveRedisTemplate<String, RefreshToken>,

    @Value("\${jwt.refresh-token-expiry-ms}")
    private val refreshTokenExpiryMs: Long
) : AbstractRedisRepository<RefreshToken>() {

    override val keyPrefix = "refreshToken:"
    private val TTL: Duration get() = Duration.ofMillis(refreshTokenExpiryMs)

    suspend fun save(userId: String, token: String): Boolean {
        val refreshToken = RefreshToken(
            userId = userId,
            token = token,
            expiry = refreshTokenExpiryMs / 1000
        )
        return save(userId, refreshToken, TTL)
    }
    suspend fun findByUserId(userId: String) = find(userId)
    suspend fun deleteByUserId(userId: String) = delete(userId)
}
