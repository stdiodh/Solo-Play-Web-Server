package com.example.solo_play_web_server.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest (
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    val password: String
)