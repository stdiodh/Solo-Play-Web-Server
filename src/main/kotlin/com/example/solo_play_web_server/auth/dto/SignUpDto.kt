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

    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,20}$",
        message = "비밀번호는 8~20자의 영문 대/소문자, 숫자, 특수문자 조합이어야 합니다.")
    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    val password : String,

    @field:Valid
    @field:NotNull(message = "약관 동의 정보는 필수입니다.")
    val agreement : Agreement,

    @field:NotBlank(message = "가입 증표는 필수 입력 항목입니다.")
    val proofToken : String
)

data class Agreement(
    @field:AssertTrue(message = "만 14세 이상이어야 합니다.")
    val isOver14 : Boolean,
    @field:AssertTrue(message = "서비스 이용 약관에 동의해야 합니다.")
    val isAgreedToTerms : Boolean,
    val isAgreedToMarketing : Boolean,
    val isConsentedToAds : Boolean
)

data class EmailAvailabilityResponse(
    val isAvailable: Boolean
)

data class EmailVerificationRequest(
    val email : String
)

data class CodeConfirmationRequest(val email: String, val code: String)

data class CodeConfirmationResponse(
    val proofToken: String
)
