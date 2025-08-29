package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.SendVerifyEmailRequest
import com.example.solo_play_web_server.auth.dto.SignUpRequest
import com.example.solo_play_web_server.auth.dto.TokenResponse
import com.example.solo_play_web_server.auth.entity.Member
import com.example.solo_play_web_server.auth.entity.PendingMember
import com.example.solo_play_web_server.auth.entity.VerificationData
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.PendingMemberRepository
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.InvalidTokenException
import com.example.solo_play_web_server.common.exception.NicknameDuplicateException
import kotlinx.coroutines.reactor.awaitSingle
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
            throw EmailDuplicateException()
        }
        if(memberRepository.existsByNickname(signUpRequest.nickname).awaitSingle()){
            throw NicknameDuplicateException()
        }

        val code = String.format("%06d", Random().nextInt(1_000_000))
        val pendingMember = PendingMember(
            email = signUpRequest.email,
            password = passwordEncoder.encode(signUpRequest.password),
            nickname = signUpRequest.nickname,
            agreement = signUpRequest.agreement
        )

        val verificationData = VerificationData(pendingMember, code)

        val success = pendingMemberRepository.save(signUpRequest.email, verificationData, Duration.ofMinutes(15))

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
            throw InvalidTokenException("인증 코드가 일치하지 않습니다.")
        }

        pendingMemberRepository.deleteByEmail(sendVerifyEmailRequest.email)

        val member = Member(
            email = verificationData.pendingMember.email,
            password = verificationData.pendingMember.password,
            nickname = verificationData.pendingMember.nickname,
            agreement = verificationData.pendingMember.agreement,
            verified = true,
            provider = AuthProvider.LOCAL,
            role = setOf(MemberRole.USER),
            imageUrl = null
        )

        val savedMember = memberRepository.save(member).awaitSingle()
        return jwtProvider.generateTokens(savedMember.id!!, savedMember.role)
    }
}