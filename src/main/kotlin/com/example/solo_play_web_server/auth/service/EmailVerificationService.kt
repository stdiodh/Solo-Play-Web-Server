package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.CodeConfirmationResponse
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.SignUpProofRepository
import com.example.solo_play_web_server.auth.repository.VerificationCodeRepository
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.VerificationCodeException
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.Random

@Service
@Transactional
class EmailVerificationService(
    private val memberRepository: MemberRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val signUpProofRepository: SignUpProofRepository,
    private val emailService: EmailService
) {
    /**
     * 신규 회원가입을 위한 인증 코드를 발송합니다.
     */
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

    /**
     * 인증 코드를 확인하고, 성공 시 최종 가입을 위한 '인증 증표'를 발급합니다.
     */
    suspend fun verifyCodeAndIssueProofToken(email: String, code: String): CodeConfirmationResponse {
        val savedCode = verificationCodeRepository.findCodeByEmail(email)

        if (savedCode == null || savedCode != code) {
            throw VerificationCodeException("인증코드가 틀렸습니다.")
        }

        if (!verificationCodeRepository.deleteByEmail(email)) {
            throw VerificationCodeException("인증코드가 만료되었거나 이미 사용되었습니다.")
        }

        val proofToken = signUpProofRepository.issueProof(email)

        return CodeConfirmationResponse(proofToken = proofToken)
    }
}
