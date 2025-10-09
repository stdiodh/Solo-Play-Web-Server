package com.example.solo_play_web_server.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            // CSRF(Cross-Site Request Forgery) 보호 비활성화 (Stateless API에서는 보통 비활성화)
            .csrf { it.disable() }
            // HTTP 기본 인증 비활성화
            .httpBasic { it.disable() }
            // 폼 기반 로그인 비활성화
            .formLogin { it.disable() }
            // 경로별 접근 권한 설정
            .authorizeExchange {
                it.pathMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                    it.pathMatchers(
                        "/api/auth/signup/**",
                        "/api/auth/check-email-duplicate",
                        "/api/auth/email-verify",
                        "/api/auth/email-confirm",
                        "/api/auth/login"
                    ).permitAll()
                    .anyExchange().authenticated()
            }
            .build()
    }
}