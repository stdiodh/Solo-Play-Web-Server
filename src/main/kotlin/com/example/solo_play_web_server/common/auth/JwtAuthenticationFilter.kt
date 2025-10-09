package com.example.solo_play_web_server.common.auth

import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = resolveToken(exchange)

        // 토큰이 존재하고 유효하다면 인증 정보를 SecurityContext에 설정
        if (token != null && tokenProvider.validateToken(token)) {
            val authentication = tokenProvider.getAuthentication(token)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }

        // 토큰이 없거나 유효하지 않으면 그냥 다음 필터로 진행
        return chain.filter(exchange)
    }

    private fun resolveToken(exchange: ServerWebExchange): String? {
        val bearerToken = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return if (bearerToken?.startsWith("Bearer ") == true) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}