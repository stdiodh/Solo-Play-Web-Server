package com.example.solo_play_web_server.auth.controller

import com.example.solo_play_web_server.auth.dto.SendVerifyEmailRequest
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원가입 Api 컨트롤러", description = "회원가입과 로그인 API 명세서 입니다.")

@RestController
@RequestMapping("/api/auth")
class MemberController(
    private val memberService: MemberService,
) {
    @Operation(
        summary = "회원가입 1단계",
        description = "회원 정보의 이메일과 비밀번호를 검증하고 이메일 코드를 보냅니다."
    )
    @PostMapping("/signup")
    suspend fun signUp (@Valid @RequestBody signUpRequest: SignUpRequest) : ResponseEntity<ApiResponse<Void>>{
        memberService.requestSignUp(signUpRequest)
        return ResponseEntity.status(HttpStatus.FOUND)
            .body(ApiResponse(status = ResultStatus.SUCCESS, message = "인증 코드를 발송했습니다. 이메일을 확인해주세요."))
    }

    @Operation(
        summary = "회원가입 2단계",
        description = "6개의 코드를 가지고 이메일 인증이 성공했다면 회원가입이 정상적으로 마무리됩니다."
    )
    @PostMapping("/verify")
    suspend fun verifyCodeAndSignUp(@Valid @RequestBody sendVerifyEmailRequest: SendVerifyEmailRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        val tokenResponse = memberService.verifyCodeAndSignUp(sendVerifyEmailRequest)
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse(status = ResultStatus.SUCCESS, message = "회원가입 및 로그인이 완료되었습니다.", data = tokenResponse))
    }
}