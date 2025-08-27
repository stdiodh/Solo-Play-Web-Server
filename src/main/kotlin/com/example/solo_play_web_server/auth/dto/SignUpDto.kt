package com.example.solo_play_web_server.auth.dto

import com.example.solo_play_web_server.auth.entity.Agreement

data class SignUpRequest(
    val email : String,
    val password : String,
    val nickname : String,
    val agreement : Agreement
)

