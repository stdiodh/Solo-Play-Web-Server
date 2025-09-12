package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.EmailAvailabilityResponse
import com.example.solo_play_web_server.auth.dto.EmailVerificationRequest
import com.example.solo_play_web_server.auth.dto.LoginRequest
import com.example.solo_play_web_server.auth.dto.SignUpRequest
import com.example.solo_play_web_server.auth.dto.TokenResponse
import com.example.solo_play_web_server.auth.entity.Member
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.VerificationCodeRepository
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.InvalidTokenException
import com.example.solo_play_web_server.common.exception.LoginFailedException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
class MemberService (
    private val memberRepository : MemberRepository,
    private val passwordEncoder : PasswordEncoder,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val emailService : EmailService,
    private val jwtProvider: JwtProvider
){
    @Transactional(readOnly = true)
    suspend fun isEmailAlreadyExists(email: String): Boolean {
        return memberRepository.existsByEmail(email).awaitSingle()
    }

    suspend fun sendVerificationCode(request: EmailVerificationRequest) {
        if (memberRepository.existsByEmail(request.email).awaitSingle()) {
            throw EmailDuplicateException(message = "이미 사용 중인 이메일입니다.")
        }

        val code = String.format("%06d", Random().nextInt(1_000_000))
        val success = verificationCodeRepository.saveCode(request.email, code, Duration.ofMinutes(10))

        if (success) {
            emailService.sendVerificationCode(request.email, code)
        } else {
            throw RuntimeException("인증 코드 저장에 실패했습니다.")
        }
    }

    suspend fun signUp(request: SignUpRequest): TokenResponse {
        val savedCode = verificationCodeRepository.findCodeByEmail(request.email)
            ?: throw InvalidTokenException("인증 코드가 만료되었거나 유효하지 않습니다.")

        if (savedCode != request.code) {
            throw InvalidTokenException("인증 코드가 일치하지 않습니다.")
        }

        val member = Member(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            agreement = request.agreement,
            provider = AuthProvider.LOCAL,
            role = setOf(MemberRole.USER)
        )

        val savedMember = memberRepository.save(member).awaitSingle()

        verificationCodeRepository.deleteCodeByEmail(request.email)

        return jwtProvider.generateTokens(savedMember.id!!, savedMember.role)
    }


    suspend fun login(loginRequest: LoginRequest) : TokenResponse {
        val member = memberRepository.findByEmail(loginRequest.email).awaitSingleOrNull()
            ?: throw LoginFailedException("존재하지 않는 계정입니다. 회원가입 하시겠습니까?")

        if (!passwordEncoder.matches(loginRequest.password, member.password)) {
            throw LoginFailedException("사용자 이름 또는 비밀번호가 올바르지 않습니다.")
        }

        return jwtProvider.generateTokens(member.id!!, member.role)
    }
}