package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.CodeConfirmationResponse
import com.example.solo_play_web_server.auth.dto.LoginRequest
import com.example.solo_play_web_server.auth.dto.SignUpRequest
import com.example.solo_play_web_server.auth.dto.TokenResponse
import com.example.solo_play_web_server.auth.entity.Member
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.SignUpProofRepository
import com.example.solo_play_web_server.auth.repository.VerificationCodeRepository
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.LoginFailedException
import com.example.solo_play_web_server.common.exception.SignUpProofException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
@Transactional
class MemberService (
    private val memberRepository : MemberRepository,
    private val passwordEncoder : PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val emailService: EmailService,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val signUpProofRepository: SignUpProofRepository
){
    @Transactional(readOnly = true)
    suspend fun isEmailAlreadyExists(email: String): Boolean {
        return memberRepository.existsByEmail(email).awaitSingle()
    }

    suspend fun sendCodeForSignUp(email: String) {
        if (memberRepository.existsByEmail(email).awaitSingle()) {
            throw EmailDuplicateException("이미 사용 중인 이메일입니다.")
        }

        val code = String.format("%06d", Random().nextInt(1_000_000))

        val success = verificationCodeRepository.saveCode(email, code, Duration.ofMinutes(10))

        if (success) {
            emailService.sendVerificationCode(email, code)
        } else {
            throw RuntimeException("인증 코드 저장에 실패했습니다.")
        }
    }

    suspend fun verifyCodeAndIssueProofToken(email: String, code: String): CodeConfirmationResponse {
        val savedCode = verificationCodeRepository.findCodeByEmail(email)

        if (savedCode == null || savedCode != code){
            return CodeConfirmationResponse(isVerified = false, proofToken = null)
        }
        verificationCodeRepository.deleteByEmail(email)

        val proofToken = signUpProofRepository.issueProof(email)

        return CodeConfirmationResponse(isVerified = true, proofToken = proofToken)
    }

    suspend fun signUp(signUpRequest: SignUpRequest) {
        val proofEmail = signUpProofRepository.consumeProof(signUpRequest.proofToken)
        if(proofEmail == null || proofEmail != signUpRequest.email){
            throw SignUpProofException("유효하지 않은 회원가입 요청입니다.")
        }

        if (memberRepository.existsByEmail(signUpRequest.email).awaitSingle()){
            throw EmailDuplicateException("이미 가입된 이메일입니다.")
        }

        val member = Member(
            email = signUpRequest.email,
            password = passwordEncoder.encode(signUpRequest.password),
            agreement = signUpRequest.agreement,
            provider = AuthProvider.LOCAL,
            role = setOf(MemberRole.USER)
        )

        memberRepository.save(member).awaitSingle()
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