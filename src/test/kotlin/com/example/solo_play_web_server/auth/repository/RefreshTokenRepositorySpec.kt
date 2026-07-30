package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.entity.RefreshToken
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import java.time.Duration

class RefreshTokenRepositorySpec : FunSpec({
    val redisTemplate = mockk<ReactiveRedisTemplate<String, RefreshToken>>()
    val valueOperations = mockk<ReactiveValueOperations<String, RefreshToken>>()
    val refreshTokenExpiryMs = 86_400_999L
    val repository = RefreshTokenRepository(redisTemplate, refreshTokenExpiryMs)

    beforeTest {
        clearAllMocks()
        every { redisTemplate.opsForValue() } returns valueOperations
    }

    test("Refresh Token은 refreshToken prefix와 밀리초 TTL 및 초 단위 expiry로 저장된다") {
        runTest {
            val expectedValue = RefreshToken(
                userId = "user-id",
                token = "refresh-token",
                expiry = refreshTokenExpiryMs / 1000
            )
            every {
                valueOperations.set(
                    "refreshToken:user-id",
                    expectedValue,
                    Duration.ofMillis(refreshTokenExpiryMs)
                )
            } returns Mono.just(true)

            repository.save("user-id", "refresh-token").shouldBeTrue()

            verify(exactly = 1) {
                valueOperations.set(
                    "refreshToken:user-id",
                    expectedValue,
                    Duration.ofMillis(refreshTokenExpiryMs)
                )
            }
        }
    }

    test("Refresh Token 저장 결과가 비어 있으면 false로 변환된다") {
        runTest {
            every {
                valueOperations.set(
                    "refreshToken:user-id",
                    any(),
                    Duration.ofMillis(refreshTokenExpiryMs)
                )
            } returns Mono.empty()

            repository.save("user-id", "refresh-token").shouldBeFalse()

            verify(exactly = 1) {
                valueOperations.set(
                    "refreshToken:user-id",
                    any(),
                    Duration.ofMillis(refreshTokenExpiryMs)
                )
            }
        }
    }

    test("Refresh Token 조회는 값 또는 null로 변환된다") {
        runTest {
            val storedToken = RefreshToken("found-user", "stored-token", 3600)
            every {
                valueOperations.get("refreshToken:found-user")
            } returns Mono.just(storedToken)
            every {
                valueOperations.get("refreshToken:missing-user")
            } returns Mono.empty()

            repository.findByUserId("found-user") shouldBe storedToken
            repository.findByUserId("missing-user").shouldBeNull()

            verify(exactly = 1) {
                valueOperations.get("refreshToken:found-user")
            }
            verify(exactly = 1) {
                valueOperations.get("refreshToken:missing-user")
            }
        }
    }

    test("Refresh Token 삭제는 Redis 결과 또는 빈 결과의 false를 반환한다") {
        runTest {
            every {
                valueOperations.delete("refreshToken:deleted-user")
            } returns Mono.just(true)
            every {
                valueOperations.delete("refreshToken:missing-user")
            } returns Mono.empty()

            repository.deleteByUserId("deleted-user").shouldBeTrue()
            repository.deleteByUserId("missing-user").shouldBeFalse()

            verify(exactly = 1) {
                valueOperations.delete("refreshToken:deleted-user")
            }
            verify(exactly = 1) {
                valueOperations.delete("refreshToken:missing-user")
            }
        }
    }
})
