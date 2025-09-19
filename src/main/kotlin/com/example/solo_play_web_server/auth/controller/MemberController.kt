package com.example.solo_play_web_server.auth.controller

import com.example.solo_play_web_server.auth.dto.CodeConfirmationRequest
import com.example.solo_play_web_server.auth.dto.CodeConfirmationResponse
import com.example.solo_play_web_server.auth.dto.EmailAvailabilityResponse
import com.example.solo_play_web_server.auth.dto.EmailVerificationRequest
import com.example.solo_play_web_server.auth.dto.LoginRequest
import com.example.solo_play_web_server.auth.dto.SignUpRequest
import com.example.solo_play_web_server.auth.dto.TokenResponse
import com.example.solo_play_web_server.auth.service.MemberService
import com.example.solo_play_web_server.common.dto.ApiResponse
import com.example.solo_play_web_server.common.dto.ResultStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원가입 Api 컨트롤러", description = "회원가입과 로그인 API 명세서 입니다.")

@RestController
@RequestMapping("/api/auth")
class MemberController(
    private val memberService: MemberService
) {
    @Operation(
        summary = "[signup] 이메일 검증",
        description = "회원의 이메일이 서버에 존재하는 지에 대한 여부를 확인합니다."
    )
    @GetMapping("/check-email-duplicate")
    suspend fun checkEmailAvailability(@RequestParam email: String): ResponseEntity<ApiResponse<EmailAvailabilityResponse>> {
        val isExist = memberService.isEmailAlreadyExists(email)

        return if (isExist) {
            val responseData = EmailAvailabilityResponse(isAvailable = false)
            val apiResponse = ApiResponse(ResultStatus.ERROR, "이미 사용 중인 아이디에요.", responseData)
            ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse)
        } else {
            val responseData = EmailAvailabilityResponse(isAvailable = true)
            val apiResponse = ApiResponse(ResultStatus.SUCCESS, "사용 가능한 이메일입니다.", responseData)
            ResponseEntity.ok(apiResponse)
        }
    }

    @Operation(summary = "[signup] 회원가입용 인증 코드 발송", description = "신규 회원가입을 위해 이메일로 인증 코드를 발송합니다.")
    @PostMapping("/email-verify")
    suspend fun sendVerificationCodeForSignUp(@Valid @RequestBody request: EmailVerificationRequest): ResponseEntity<ApiResponse<Unit>> {
        memberService.sendCodeForSignUp(request.email)
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse(status = ResultStatus.SUCCESS, message = "인증 코드를 발송했습니다. 이메일을 확인해주세요."))
    }


    @Operation(
        summary = "[signup] 회원가입용 인증 코드 확인 및 증표 발급",
        description = "신규 회원가입을 위해 이메일로 인증 코드를 발송합니다."
    )
    @PostMapping("/email-confirm")
    suspend fun sendVerificationCodeForSignUp(@Valid @RequestBody codeConfirmationRequest: CodeConfirmationRequest):
            ResponseEntity<ApiResponse<CodeConfirmationResponse>>{
        val responseData = memberService.verifyCodeAndIssueProofToken(codeConfirmationRequest.email, codeConfirmationRequest.code)
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse(status = ResultStatus.SUCCESS, message = "인증 코드가 확인되었습니다.", responseData))
    }


    @Operation(
        summary = "[signup] 회원가입",
        description = "인증된 이메일과 모든 회원 정보 그리고 인증 증표를 제출하여 가입이 완료됩니다."
    )
    @PostMapping("/signup")
    suspend fun signUp(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<ApiResponse<Unit>>{
        memberService.signUp(signUpRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(status = ResultStatus.SUCCESS, message = "회원가입이 완료되었습니다.")
        )
    }

    @Operation(
        summary = "[login] 로그인",
        description = "회원의 이메일과 비밀번호를 받아 인증에 성공한다면 엑세스, 리프래쉬 토큰을 발급합니다."
    )
    @PostMapping("/login")
    suspend fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<ApiResponse<TokenResponse>>{
        val tokenResponse = memberService.login(loginRequest)
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse(status = ResultStatus.SUCCESS, message = "로그인에 성공했습니다.", data = tokenResponse))
    }

}