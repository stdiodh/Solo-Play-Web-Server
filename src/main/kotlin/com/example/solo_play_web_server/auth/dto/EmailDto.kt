package com.example.solo_play_web_server.auth.dto

data class SendVerifyEmailRequest (
    val email : String
)

data class SendVerifyEmailResponse (
    val message : String
)
