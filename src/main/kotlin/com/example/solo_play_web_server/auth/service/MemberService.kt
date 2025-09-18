package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.*
import com.example.solo_play_web_server.auth.entity.Member
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.PendingMemberRepository
import com.example.solo_play_web_server.auth.repository.VerificationCodeRepository
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.InvalidTokenException
import com.example.solo_play_web_server.common.exception.LoginFailedException
import com.example.solo_play_web_server.common.exception.VerificationCodeException
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
    private val jwtProvider: JwtProvider,
    private val pendingMemberRepository: PendingMemberRepository
){
    @Transactional(readOnly = true)
    suspend fun isEmailAlreadyExists(email: String): Boolean {
        return memberRepository.existsByEmail(email).awaitSingle()
    }

    @Transactional
    suspend fun provisionalSignUp(provisionalSignUpRequest: ProvisionalSignUpRequest) {
        if (memberRepository.findByEmail(provisionalSignUpRequest.email).awaitSingleOrNull() != null) {
            throw EmailDuplicateException("이미 가입된 이메일입니다.")
        }

        val pendingData = PendingMemberData(
            email = provisionalSignUpRequest.email,
            encodedPassword = passwordEncoder.encode(provisionalSignUpRequest.password),
            agreement = provisionalSignUpRequest.agreement
        )
        pendingMemberRepository.save(pendingData)

        val code = String.format("%06d", Random().nextInt(1_000_000))

        val success = verificationCodeRepository.saveCode(provisionalSignUpRequest.email, code, Duration.ofMinutes(10))

        if (success) {
            emailService.sendVerificationCode(provisionalSignUpRequest.email, code)
        } else {
            throw RuntimeException("인증 코드 저장에 실패했습니다. 잠시 후 다시 시도해주세요.")
        }
    }

    @Transactional
    suspend fun finalizeSignUp(finalizeSignUpRequest: FinalizeSignUpRequest): TokenResponse {
        val savedCode = verificationCodeRepository.findCodeByEmail(finalizeSignUpRequest.email)
            ?: throw InvalidTokenException("인증 코드가 만료되었거나 유효하지 않습니다.")
        if (savedCode != finalizeSignUpRequest.code) {
            throw InvalidTokenException("인증 코드가 일치하지 않습니다.")
        }

        val pendingData = pendingMemberRepository.findByEmail(finalizeSignUpRequest.email)
            ?: throw VerificationCodeException("회원가입 시간이 만료되었거나 유효하지 않은 요청입니다.")

        val member = Member(
            email = pendingData.email,
            password = pendingData.encodedPassword,
            agreement = pendingData.agreement,
            provider = AuthProvider.LOCAL,
            role = setOf(MemberRole.USER)
        )
        val savedMember = memberRepository.save(member).awaitSingle()

        pendingMemberRepository.deleteByEmail(finalizeSignUpRequest.email)
        verificationCodeRepository.deleteByEmail(finalizeSignUpRequest.email)

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