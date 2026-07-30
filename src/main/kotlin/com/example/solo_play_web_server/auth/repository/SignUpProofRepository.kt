package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.common.repository.AbstractRedisRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.UUID

@Repository
class SignUpProofRepository(
    @Qualifier("signUpProofRedisTemplate")
    override val redisTemplate: ReactiveRedisTemplate<String, String>
) : AbstractRedisRepository<String>() {

    override val keyPrefix = "SIGNUP_PROOF:"
    private val TTL = Duration.ofMinutes(10)

    suspend fun issueProof(email: String): String {
        val proofToken = UUID.randomUUID().toString()
        if (!save(proofToken, email, TTL)) {
            throw IllegalStateException("회원가입 증표 저장에 실패했습니다.")
        }
        return proofToken
    }

    suspend fun consumeProof(proofToken: String): String? {
        // 부모의 findAndDelete 메서드를 호출
        return findAndDelete(proofToken)
    }
}
