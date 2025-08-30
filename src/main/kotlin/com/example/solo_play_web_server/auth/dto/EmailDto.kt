package com.example.solo_play_web_server.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SendVerifyEmailRequest (
    val email : String,
    @field:NotBlank(message = "인증 코드 6자리를 입력해주세요.")
    @field:Pattern(regexp = "^[0-9]{6}$",
        message = "인증 코드는 숫자 6자리입니다!")
    val code : String
)
