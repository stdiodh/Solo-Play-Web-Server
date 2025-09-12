package com.example.solo_play_web_server.auth.controller

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
    private val memberService: MemberService,
) {
    @Operation(
        summary = "[signup] 이메일 검증",
        description = "회원의 이메일이 서버에 존재하는 지에 대한 여부를 확인합니다."
    )
    @GetMapping("/email-check")
    suspend fun checkEmailAvailability(@RequestParam email: String): ResponseEntity<ApiResponse<EmailAvailabilityResponse>> {
        val isExist = memberService.isEmailAlreadyExists(email)
        val isAvailable = !isExist
        val responseData = EmailAvailabilityResponse(isAvailable = isAvailable)
        val message = if (isAvailable) "사용 가능한 이메일입니다." else "이미 사용 중인 아이디에요."

        return ResponseEntity.ok(ApiResponse(ResultStatus.SUCCESS, message, responseData))
    }

    @Operation(
        summary = "[signup] 이메일 인증 코드 전송",
        description = "회원의 이메일에 인증 메일을 발송합니다."
    )
    @PostMapping("/email-verify")
    suspend fun sendVerificationEmail(@Valid @RequestBody emailVerificationRequest: EmailVerificationRequest): ResponseEntity<ApiResponse<Void>> {
        memberService.sendVerificationCode(emailVerificationRequest)
        return ResponseEntity.ok(ApiResponse(ResultStatus.SUCCESS,"인증 코드를 발송했습니다. 이메일을 확인해주세요."))
    }

    @Operation(
        summary = "[signup] 최종 회원가입",
        description = "검증된 회원 정보와 이메일 인증이 성공했다면 회원가입이 성공되며 엑세스 토큰과 리프레쉬 토큰이 발급됩니다."
    )
    @PostMapping("/signup")
    suspend fun signUp(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        val tokenResponse = memberService.signUp(signUpRequest)
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