package com.example.solo_play_web_server.auth.dto

data class SendVerificationEmailRequest (
    val email : String
)

data class SendVerificationEmailResponse (
    val message : String
)
