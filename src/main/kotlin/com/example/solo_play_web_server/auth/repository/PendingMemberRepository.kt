package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.dto.PendingMemberData
import com.example.solo_play_web_server.common.repository.AbstractRedisRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class PendingMemberRepository(
    @Qualifier("pendingMemberDataRedisTemplate")
    override val redisTemplate: ReactiveRedisTemplate<String, PendingMemberData>
) : AbstractRedisRepository<PendingMemberData>() {

    override val keyPrefix = "PENDING_SIGNUP:"
    private val TTL = Duration.ofMinutes(10)

    suspend fun save(pendingMemberData: PendingMemberData) = save(pendingMemberData.email, pendingMemberData, TTL)
    suspend fun findByEmail(email: String) = find(email)
    suspend fun deleteByEmail(email: String) = delete(email)
}