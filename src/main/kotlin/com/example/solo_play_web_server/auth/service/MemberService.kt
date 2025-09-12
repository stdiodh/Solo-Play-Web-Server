package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.LoginRequest
import com.example.solo_play_web_server.auth.dto.PendingMember
import com.example.solo_play_web_server.auth.dto.SendVerifyEmailRequest
import com.example.solo_play_web_server.auth.dto.SignUpRequest
import com.example.solo_play_web_server.auth.dto.TokenResponse
import com.example.solo_play_web_server.auth.dto.VerificationData
import com.example.solo_play_web_server.auth.entity.Member
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.PendingMemberRepository
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.InvalidTokenException
import com.example.solo_play_web_server.common.exception.LoginFailedException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Random

@Service
class MemberService (
    private val memberRepository : MemberRepository,
    private val passwordEncoder : PasswordEncoder,
    private val pendingMemberRepository: PendingMemberRepository,
    private val emailService : EmailService,
    private val jwtProvider: JwtProvider
){
    suspend fun requestSignUp(signUpRequest: SignUpRequest) {
        if(memberRepository.existsByEmail(signUpRequest.email).awaitSingle()){
            throw EmailDuplicateException(message = "이미 사용 중인 이메일입니다.")
        }

        val code = String.format("%06d", Random().nextInt(1_000_000))
        val pendingMember = PendingMember(
            email = signUpRequest.email,
            password = passwordEncoder.encode(signUpRequest.password),
            agreement = signUpRequest.agreement
        )

        val verificationData = VerificationData(pendingMember, code)

        val success = pendingMemberRepository.save(signUpRequest.email, verificationData, Duration.ofMinutes(10))

        if(success) {
            emailService.sendVerificationCode(signUpRequest.email, code)
        } else {
            throw RuntimeException("임시 회원 저장에 실패했습니다.")
        }
    }

    suspend fun verifyCodeAndSignUp(sendVerifyEmailRequest : SendVerifyEmailRequest) : TokenResponse {
        val verificationData = pendingMemberRepository.findByEmail(sendVerifyEmailRequest.email)
            ?: throw InvalidTokenException("인증 시간이 만료되었거나 요청 정보가 잘못되었습니다.")

        if(verificationData.code != sendVerifyEmailRequest.code){
            throw InvalidTokenException("인증코드가 틀렸습니다.")
        }

        pendingMemberRepository.deleteByEmail(sendVerifyEmailRequest.email)

        val member = Member(
            email = verificationData.pendingMember.email,
            password = verificationData.pendingMember.password,
            agreement = verificationData.pendingMember.agreement,
            verified = true,
            provider = AuthProvider.LOCAL,
            role = setOf(MemberRole.USER),
            imageUrl = null
        )

        val savedMember = memberRepository.save(member).awaitSingle()
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