package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.entity.RefreshToken
import reactor.core.publisher.Mono

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): Mono<RefreshToken>
    fun findByUserId(userId: Long): Mono<RefreshToken>
}