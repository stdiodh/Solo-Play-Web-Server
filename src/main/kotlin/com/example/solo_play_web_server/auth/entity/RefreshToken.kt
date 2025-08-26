package com.example.solo_play_web_server.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("refreshToken")
data class RefreshToken (
    @Id
    val userId : String,
    val token : String,
    @TimeToLive
    val expiry : Long
)