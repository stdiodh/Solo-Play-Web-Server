package com.example.solo_play_web_server.auth.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class SignUpRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수 입력 항목입니다.")
    val email : String,

    @field:Pattern(regexp ="^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()])[a-zA-Z0-9!@#\$%^&*()]{8,16}\$",
        message = "비밀번호는 8~16자의 영문, 숫자, 특수문자 조합이어야 합니다.")
    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    val password : String,

    @field:NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    val nickname : String,

    @field:Valid
    @field:NotNull(message = "약관 동의 정보는 필수입니다.")
    val agreement : Agreement
)

data class Agreement(
    @field:AssertTrue(message = "만 14세 이상이어야 합니다.")
    val isOver14 : Boolean,
    @field:AssertTrue(message = "서비스 이용 약관에 동의해야 합니다.")
    val isAgreedToTerms : Boolean,
    val isAgreedToMarketing : Boolean,
    val isConsentedToAds : Boolean
)

data class PendingMember(
    val email: String,
    val password: String,
    val nickname: String,
    val agreement: Agreement
)

data class VerificationData (
    val pendingMember: PendingMember,
    val code: String
)