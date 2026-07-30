package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.common.repository.AbstractRedisRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class VerificationCodeRepository(
    @Qualifier("verificationCodeRedisTemplate")
    override val redisTemplate: ReactiveRedisTemplate<String, String>
) : AbstractRedisRepository<String>() {

    override val keyPrefix = "verificationCode:"

    suspend fun saveCode(email: String, code: String, expiry: Duration) = save(email, code, expiry)
    suspend fun findCodeByEmail(email: String) = find(email)
    suspend fun deleteByEmail(email: String) = delete(email)
}