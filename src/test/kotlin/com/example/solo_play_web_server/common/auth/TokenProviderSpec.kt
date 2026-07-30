package com.example.solo_play_web_server.common.auth

import com.example.solo_play_web_server.auth.enum.MemberRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.springframework.security.core.userdetails.User
import java.nio.charset.StandardCharsets
import java.util.Date

class TokenProviderSpec : FunSpec({
    val secretKey = "a-very-long-and-secure-secret-key-for-token-provider-tests"
    val otherSecretKey = "a-different-long-and-secure-secret-key-for-provider-tests"
    val tokenProvider = TokenProvider(secretKey).apply { init() }

    test("유효한 토큰에서 subject와 복수 role을 복원한다") {
        val token = createToken(
            secretKey = secretKey,
            subject = "user-id",
            auth = "USER,ADMIN"
        )

        tokenProvider.validateToken(token).shouldBeTrue()
        val authentication = tokenProvider.getAuthentication(token)

        authentication.name shouldBe "user-id"
        (authentication.principal as User).username shouldBe "user-id"
        authentication.authorities.map { it.authority }
            .shouldContainExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
        authentication.credentials shouldBe token
    }

    test("auth claim이 없으면 유효하지 않다") {
        val token = createToken(
            secretKey = secretKey,
            subject = "user-id",
            includeAuth = false
        )

        tokenProvider.validateToken(token).shouldBeFalse()
    }

    test("auth claim이 없는 Refresh Token은 Access Token으로 사용할 수 없다") {
        val jwtProvider = JwtProvider(
            secretKey = secretKey,
            accessTokenExpiryMs = 60_000,
            refreshTokenExpiryMs = 120_000
        ).apply { init() }
        val tokens = jwtProvider.generateTokens("user-id", setOf(MemberRole.USER))

        tokenProvider.validateToken(tokens.accessToken).shouldBeTrue()
        tokenProvider.validateToken(tokens.refreshToken).shouldBeFalse()
    }

    test("auth claim이 빈 값이면 유효하지 않다") {
        val emptyAuthToken = createToken(secretKey, "user-id", auth = "")
        val blankAuthToken = createToken(secretKey, "user-id", auth = "   ")

        tokenProvider.validateToken(emptyAuthToken).shouldBeFalse()
        tokenProvider.validateToken(blankAuthToken).shouldBeFalse()
    }

    test("auth claim이 구분자와 빈 segment로만 구성되면 유효하지 않다") {
        val delimiterOnlyToken = createToken(secretKey, "user-id", auth = ",")
        val blankSegmentsToken = createToken(secretKey, "user-id", auth = " ,   , ")

        tokenProvider.validateToken(delimiterOnlyToken).shouldBeFalse()
        tokenProvider.validateToken(blankSegmentsToken).shouldBeFalse()
    }

    test("만료된 토큰은 유효하지 않다") {
        val token = createToken(
            secretKey = secretKey,
            subject = "user-id",
            auth = "USER",
            issuedAt = Date(System.currentTimeMillis() - 120_000),
            expiration = Date(System.currentTimeMillis() - 60_000)
        )

        tokenProvider.validateToken(token).shouldBeFalse()
    }

    test("서명이 변조된 토큰은 유효하지 않다") {
        val token = createToken(secretKey, "user-id", auth = "USER")
        val parts = token.split(".")
        val signature = parts[2]
        val changedFirstCharacter = if (signature.first() == 'a') 'b' else 'a'
        val tamperedToken = "${parts[0]}.${parts[1]}.$changedFirstCharacter${signature.drop(1)}"

        tokenProvider.validateToken(tamperedToken).shouldBeFalse()
    }

    test("다른 키로 서명한 토큰은 유효하지 않다") {
        val token = createToken(otherSecretKey, "user-id", auth = "USER")

        tokenProvider.validateToken(token).shouldBeFalse()
    }

    test("malformed token은 유효하지 않다") {
        tokenProvider.validateToken("not-a-jwt").shouldBeFalse()
    }
})

private fun createToken(
    secretKey: String,
    subject: String,
    auth: String = "USER",
    includeAuth: Boolean = true,
    issuedAt: Date = Date(),
    expiration: Date = Date(System.currentTimeMillis() + 60_000)
): String {
    val builder = Jwts.builder()
        .subject(subject)
        .issuedAt(issuedAt)
        .expiration(expiration)

    if (includeAuth) {
        builder.claim("auth", auth)
    }

    return builder
        .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8)))
        .compact()
}
