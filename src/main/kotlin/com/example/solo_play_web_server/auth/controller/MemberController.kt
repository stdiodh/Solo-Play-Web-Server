package com.example.solo_play_web_server.auth.controller

import com.example.solo_play_web_server.auth.dto.EmailAvailabilityResponse
import com.example.solo_play_web_server.auth.dto.EmailVerificationRequest
import com.example.solo_play_web_server.auth.dto.FinalizeSignUpRequest
import com.example.solo_play_web_server.auth.dto.LoginRequest
import com.example.solo_play_web_server.auth.dto.ProvisionalSignUpRequest
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
    private val memberService: MemberService,
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

    @Operation(summary = "[signup] 1. 임시 회원가입 및 이메일 인증 요청", description = "회원 정보를 받아 임시 저장하고, 해당 이메일로 인증 코드를 발송합니다.")
    @PostMapping("/signup/provisional")
    suspend fun provisionalSignUp(@Valid @RequestBody provisionalSignUpRequest: ProvisionalSignUpRequest): ResponseEntity<ApiResponse<Unit>> {
        memberService.provisionalSignUp(provisionalSignUpRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(ResultStatus.SUCCESS, "인증 메일이 발송되었습니다. 이메일을 확인해주세요."))
    }

    @Operation(summary = "[signup] 2. 최종 회원가입", description = "이메일과 인증 코드로 최종 인증을 완료하고 토큰을 발급합니다.")
    @PostMapping("/signup/finalize")
    suspend fun finalizeSignUp(@Valid @RequestBody finalizeSignUpRequest: FinalizeSignUpRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        val tokenResponse = memberService.finalizeSignUp(finalizeSignUpRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(ResultStatus.SUCCESS, "회원가입이 완료되었습니다.", tokenResponse))
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