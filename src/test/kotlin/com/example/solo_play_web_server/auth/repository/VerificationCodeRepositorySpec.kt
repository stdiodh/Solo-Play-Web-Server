package com.example.solo_play_web_server.auth.repository

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

class VerificationCodeRepositorySpec : FunSpec({
    val redisTemplate = mockk<ReactiveRedisTemplate<String, String>>()
    val valueOperations = mockk<ReactiveValueOperations<String, String>>()
    val repository = VerificationCodeRepository(redisTemplate)

    beforeTest {
        clearAllMocks()
        every { redisTemplate.opsForValue() } returns valueOperations
    }

    test("인증 코드는 전달된 TTL과 verificationCode prefix로 저장된다") {
        runTest {
            val email = "member@example.com"
            val code = "012345"
            val expiry = Duration.ofMinutes(10)
            every {
                valueOperations.set("verificationCode:$email", code, expiry)
            } returns Mono.just(true)

            repository.saveCode(email, code, expiry).shouldBeTrue()

            verify(exactly = 1) {
                valueOperations.set("verificationCode:$email", code, expiry)
            }
        }
    }

    test("인증 코드 저장 결과가 비어 있으면 false로 변환된다") {
        runTest {
            val expiry = Duration.ofMinutes(3)
            every {
                valueOperations.set("verificationCode:member@example.com", "123456", expiry)
            } returns Mono.empty()

            repository.saveCode("member@example.com", "123456", expiry).shouldBeFalse()
        }
    }

    test("인증 코드 조회는 값 또는 null로 변환된다") {
        runTest {
            every {
                valueOperations.get("verificationCode:found@example.com")
            } returns Mono.just("654321")
            every {
                valueOperations.get("verificationCode:missing@example.com")
            } returns Mono.empty()

            repository.findCodeByEmail("found@example.com") shouldBe "654321"
            repository.findCodeByEmail("missing@example.com").shouldBeNull()

            verify(exactly = 1) {
                valueOperations.get("verificationCode:found@example.com")
            }
            verify(exactly = 1) {
                valueOperations.get("verificationCode:missing@example.com")
            }
        }
    }

    test("인증 코드 삭제는 Redis 결과 또는 빈 결과의 false를 반환한다") {
        runTest {
            every {
                valueOperations.delete("verificationCode:deleted@example.com")
            } returns Mono.just(true)
            every {
                valueOperations.delete("verificationCode:missing@example.com")
            } returns Mono.empty()

            repository.deleteByEmail("deleted@example.com").shouldBeTrue()
            repository.deleteByEmail("missing@example.com").shouldBeFalse()

            verify(exactly = 1) {
                valueOperations.delete("verificationCode:deleted@example.com")
            }
            verify(exactly = 1) {
                valueOperations.delete("verificationCode:missing@example.com")
            }
        }
    }
})
