package com.example.solo_play_web_server.member.controller

import com.example.solo_play_web_server.common.dto.BaseResponse
import com.example.solo_play_web_server.member.dto.SignUpDto
import com.example.solo_play_web_server.member.service.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원 Api 컨트롤러", description = "회원가입 Api, 로그인, 회원조회 명세서입니다.")
@RestController
@RequestMapping("/api/member")
class MemberController @Autowired constructor(private val memberService: MemberService) {
    @Operation(summary = "사용자 정보 회원가입", description = "사용자 회원가입 API 입니다.")
    @PostMapping("/signup")
    suspend fun signup(@Parameter(description = "회원가입 정보 데이터") @RequestBody signUpDto: SignUpDto)
            : ResponseEntity<BaseResponse<String>> {
        val result = memberService.signup(signUpDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            BaseResponse(
                data = result
            )
        )
    }
}