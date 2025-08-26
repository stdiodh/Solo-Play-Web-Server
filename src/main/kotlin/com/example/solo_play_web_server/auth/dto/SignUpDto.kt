package com.example.solo_play_web_server.auth.dto

data class SignUpRequest(
    val email : String,
    val password : String,
    val nickname : String,
    val isOver14 : Boolean,
    val isAgreedToTerms : Boolean,
    val isAgreedToMarketing : Boolean,
    val isConsentedToAds : Boolean
)

