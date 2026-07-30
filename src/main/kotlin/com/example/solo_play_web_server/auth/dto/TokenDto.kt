package com.example.solo_play_web_server.auth.dto

data class TokenResponse (
    val grantType : String = "Bearer",
    val accessToken : String,
    val refreshToken : String
)
