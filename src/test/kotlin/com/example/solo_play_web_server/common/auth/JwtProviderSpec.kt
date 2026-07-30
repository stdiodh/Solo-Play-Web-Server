package com.example.solo_play_web_server.common.auth

import com.example.solo_play_web_server.auth.enum.MemberRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.date.shouldBeBetween
import io.kotest.matchers.shouldBe
import java.nio.charset.StandardCharsets
import java.util.*

class JwtProviderSpec : BehaviorSpec({

    val testSecretKey = "a-very-long-and-secure-secret-key-for-testing-purpose-only"
    val accessTokenExpiryMs = 3600000L // 1시간
    val refreshTokenExpiryMs = 86400000L // 24시간

    val jwtProvider = JwtProvider(
        secretKey = testSecretKey,
        accessTokenExpiryMs = accessTokenExpiryMs,
        refreshTokenExpiryMs = refreshTokenExpiryMs
    ).apply { init() }

    val testKey = Keys.hmacShaKeyFor(testSecretKey.toByteArray(StandardCharsets.UTF_8))

    Given("JwtProvider가 토큰을 생성할 때") {
        val userId = "user-id-123"
        val roles = setOf(MemberRole.USER, MemberRole.ADMIN)

        When("사용자 ID와 역할을 전달하면") {
            val now = Date()
            val tokens = jwtProvider.generateTokens(userId, roles)

            Then("액세스 토큰이 유효하고 올바른 만료 시간을 가져야 한다") {
                val claims = Jwts.parser().verifyWith(testKey).build()
                    .parseSignedClaims(tokens.accessToken).payload

                claims.subject shouldBe userId
                claims["auth"] shouldBe "USER,ADMIN"

                val actualExpiry = claims.expiration.toInstant()
                val expectedExpiry = Date(now.time + accessTokenExpiryMs)

                val lowerBound = Date(expectedExpiry.time - 1000).toInstant()
                val upperBound = Date(expectedExpiry.time + 1000).toInstant()

                actualExpiry.shouldBeBetween(lowerBound, upperBound)
            }

            Then("리프레시 토큰이 유효하고 올바른 만료 시간을 가져야 한다") {
                val claims = Jwts.parser().verifyWith(testKey).build()
                    .parseSignedClaims(tokens.refreshToken).payload

                claims.subject shouldBe userId

                val actualExpiry = claims.expiration.toInstant()
                val expectedExpiry = Date(now.time + refreshTokenExpiryMs)

                val lowerBound = Date(expectedExpiry.time - 1000).toInstant()
                val upperBound = Date(expectedExpiry.time + 1000).toInstant()

                actualExpiry.shouldBeBetween(lowerBound, upperBound)
            }
        }
    }
})
