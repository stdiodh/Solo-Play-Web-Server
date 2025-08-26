package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.entity.RefreshToken
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface RefreshTokenRepository : ReactiveCrudRepository<RefreshToken, String>{
    fun findByUserId(userId: Long) : Mono<RefreshToken>
}