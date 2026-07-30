package com.example.solo_play_web_server.auth.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
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
import java.util.UUID

class SignUpProofRepositorySpec : FunSpec({
    val redisTemplate = mockk<ReactiveRedisTemplate<String, String>>()
    val valueOperations = mockk<ReactiveValueOperations<String, String>>()
    val repository = SignUpProofRepository(redisTemplate)

    beforeTest {
        clearAllMocks()
        every { redisTemplate.opsForValue() } returns valueOperations
    }

    test("가입 증표는 SIGNUP_PROOF prefix와 10분 TTL로 저장된다") {
        runTest {
            val email = "member@example.com"
            every {
                valueOperations.set(match { it.startsWith("SIGNUP_PROOF:") }, email, Duration.ofMinutes(10))
            } returns Mono.just(true)

            val proofToken = repository.issueProof(email)

            UUID.fromString(proofToken).toString() shouldBe proofToken
            verify(exactly = 1) {
                valueOperations.set("SIGNUP_PROOF:$proofToken", email, Duration.ofMinutes(10))
            }
        }
    }

    test("가입 증표 저장 결과가 비어 있으면 발급에 실패한다") {
        runTest {
            val email = "member@example.com"
            every {
                valueOperations.set(match { it.startsWith("SIGNUP_PROOF:") }, email, Duration.ofMinutes(10))
            } returns Mono.empty()

            shouldThrow<IllegalStateException> {
                repository.issueProof(email)
            }

            verify(exactly = 1) {
                valueOperations.set(match { it.startsWith("SIGNUP_PROOF:") }, email, Duration.ofMinutes(10))
            }
        }
    }

    test("가입 증표는 getAndDelete 한 번으로 원자 소비된다") {
        runTest {
            val proofToken = "proof-token"
            every {
                valueOperations.getAndDelete("SIGNUP_PROOF:$proofToken")
            } returns Mono.just("member@example.com")

            repository.consumeProof(proofToken) shouldBe "member@example.com"

            verify(exactly = 1) {
                valueOperations.getAndDelete("SIGNUP_PROOF:$proofToken")
            }
            verify(exactly = 0) {
                valueOperations.get(any())
            }
            verify(exactly = 0) {
                valueOperations.delete(any())
            }
        }
    }

    test("존재하지 않는 가입 증표의 원자 소비 결과는 null이다") {
        runTest {
            every {
                valueOperations.getAndDelete("SIGNUP_PROOF:missing-proof")
            } returns Mono.empty()

            repository.consumeProof("missing-proof").shouldBeNull()

            verify(exactly = 1) {
                valueOperations.getAndDelete("SIGNUP_PROOF:missing-proof")
            }
        }
    }
})
