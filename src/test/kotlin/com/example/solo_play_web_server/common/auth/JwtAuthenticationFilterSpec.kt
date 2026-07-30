package com.example.solo_play_web_server.common.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.test.StepVerifier
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class JwtAuthenticationFilterSpec : FunSpec({
    val tokenProvider = mockk<TokenProvider>()
    val filter = JwtAuthenticationFilter(tokenProvider)

    beforeTest {
        clearAllMocks()
    }

    test("Authorization 헤더가 없으면 인증 없이 filter chain을 한 번 호출한다") {
        val exchange = exchangeWithAuthorization(null)
        val authentication = AtomicReference<Authentication?>()
        val chainCalls = AtomicInteger()

        StepVerifier.create(filter.filter(exchange, recordingChain(authentication, chainCalls)))
            .verifyComplete()

        chainCalls.get() shouldBe 1
        authentication.get().shouldBeNull()
        verify(exactly = 0) { tokenProvider.validateToken(any()) }
        verify(exactly = 0) { tokenProvider.getAuthentication(any()) }
    }

    test("Basic Authorization이면 인증 없이 filter chain을 한 번 호출한다") {
        val credentials = listOf("test-user", "test-password").joinToString(":")
        val encodedCredentials = Base64.getEncoder()
            .encodeToString(credentials.toByteArray(StandardCharsets.UTF_8))
        val exchange = exchangeWithAuthorization("Basic $encodedCredentials")
        val authentication = AtomicReference<Authentication?>()
        val chainCalls = AtomicInteger()

        StepVerifier.create(filter.filter(exchange, recordingChain(authentication, chainCalls)))
            .verifyComplete()

        chainCalls.get() shouldBe 1
        authentication.get().shouldBeNull()
        verify(exactly = 0) { tokenProvider.validateToken(any()) }
        verify(exactly = 0) { tokenProvider.getAuthentication(any()) }
    }

    test("잘못된 Bearer 토큰이면 인증 없이 filter chain을 한 번 호출한다") {
        every { tokenProvider.validateToken("invalid-token") } returns false
        val exchange = exchangeWithAuthorization("Bearer invalid-token")
        val authentication = AtomicReference<Authentication?>()
        val chainCalls = AtomicInteger()

        StepVerifier.create(filter.filter(exchange, recordingChain(authentication, chainCalls)))
            .verifyComplete()

        chainCalls.get() shouldBe 1
        authentication.get().shouldBeNull()
        verify(exactly = 1) { tokenProvider.validateToken("invalid-token") }
        verify(exactly = 0) { tokenProvider.getAuthentication(any()) }
    }

    test("유효한 Bearer 토큰이면 SecurityContext에 인증을 넣고 filter chain을 한 번 호출한다") {
        val expectedAuthentication = UsernamePasswordAuthenticationToken(
            User("user-id", "", listOf(SimpleGrantedAuthority("ROLE_USER"))),
            "valid-token",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
        every { tokenProvider.validateToken("valid-token") } returns true
        every { tokenProvider.getAuthentication("valid-token") } returns expectedAuthentication
        val exchange = exchangeWithAuthorization("Bearer valid-token")
        val authentication = AtomicReference<Authentication?>()
        val chainCalls = AtomicInteger()

        StepVerifier.create(filter.filter(exchange, recordingChain(authentication, chainCalls)))
            .verifyComplete()

        chainCalls.get() shouldBe 1
        authentication.get() shouldBe expectedAuthentication
        verify(exactly = 1) { tokenProvider.validateToken("valid-token") }
        verify(exactly = 1) { tokenProvider.getAuthentication("valid-token") }
    }
})

private fun exchangeWithAuthorization(authorization: String?): MockServerWebExchange {
    val request = MockServerHttpRequest.get("/protected")
    if (authorization != null) {
        request.header(HttpHeaders.AUTHORIZATION, authorization)
    }
    return MockServerWebExchange.from(request.build())
}

private fun recordingChain(
    authentication: AtomicReference<Authentication?>,
    calls: AtomicInteger
): WebFilterChain {
    return WebFilterChain { _: ServerWebExchange ->
        calls.incrementAndGet()
        ReactiveSecurityContextHolder.getContext()
            .doOnNext { authentication.set(it.authentication) }
            .then()
    }
}
