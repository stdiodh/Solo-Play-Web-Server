package com.example.solo_play_web_server.common.config

import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.common.auth.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            .authorizeExchange {
                it.pathMatchers(
                    "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**",
                    "/api/auth/signup/**", "/api/auth/check-email-duplicate",
                    "/api/auth/email-verify", "/api/auth/email-confirm",
                    "/api/auth/login"
                ).permitAll()

                    .pathMatchers(HttpMethod.GET, "/api/places/recommendations")
                    .hasRole(MemberRole.USER.name)

                    .anyExchange().authenticated()
            }
            .build()
    }
}