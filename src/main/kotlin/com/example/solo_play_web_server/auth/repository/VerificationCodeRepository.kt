package com.example.solo_play_web_server.auth.repository

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class VerificationCodeRepository (
    @Qualifier("verificationCodeRedisTemplate")
    private val redisTemplate: ReactiveRedisTemplate<String, String>
){
    private fun getKey(email: String) = "verificationCode:$email"

    suspend fun saveCode(email: String, code: String, expiry: Duration): Boolean {
        return redisTemplate.opsForValue().set(getKey(email), code, expiry).awaitSingleOrNull() ?: false
    }

    suspend fun findCodeByEmail(email: String): String? {
        return redisTemplate.opsForValue().get(getKey(email)).awaitSingleOrNull()
    }

    // 6. 메서드 이름도 역할에 맞게 변경합니다. (로직은 동일)
    suspend fun deleteCodeByEmail(email: String) {
        redisTemplate.opsForValue().delete(getKey(email)).awaitSingleOrNull()
    }
}