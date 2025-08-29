package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.entity.VerificationData
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class PendingMemberRepository (
    @Qualifier("verificationDataRedisTemplate")
    private val redisTemplate: ReactiveRedisTemplate<String, VerificationData>
){
    private fun getKey(token: String) = "pendingMember:$token"

    suspend fun save(token: String, verificationData: VerificationData, expiry: Duration) : Boolean {
        return redisTemplate.opsForValue().set(getKey(token), verificationData, expiry).awaitSingleOrNull() ?: false
    }

    suspend fun findByEmail(email: String) : VerificationData? {
        return redisTemplate.opsForValue().get(getKey(email)).awaitSingleOrNull()
    }

    suspend fun deleteByEmail(email: String) {
        redisTemplate.opsForValue().delete(getKey(email)).awaitSingleOrNull()
    }
}