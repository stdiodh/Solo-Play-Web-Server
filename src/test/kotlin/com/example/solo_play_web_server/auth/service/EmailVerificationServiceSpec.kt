package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.CodeConfirmationResponse
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.SignUpProofRepository
import com.example.solo_play_web_server.auth.repository.VerificationCodeRepository
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import reactor.core.publisher.Mono

class EmailVerificationServiceSpec : BehaviorSpec() {
    @MockK
    lateinit var memberRepository: MemberRepository
    @MockK
    lateinit var verificationCodeRepository: VerificationCodeRepository
    @MockK
    lateinit var signUpProofRepository: SignUpProofRepository
    @MockK
    lateinit var emailService: EmailService

    @InjectMockKs
    lateinit var emailVerificationService: EmailVerificationService

    init {
        beforeTest { MockKAnnotations.init(this) }
        afterTest { clearAllMocks() }

        Given("회원가입용 인증 코드 발송(sendCodeForSignUp) 시") {
            val email = "test@example.com"
            When("사용 가능한 이메일이면") {
                coEvery { memberRepository.existsByEmail(email) } returns Mono.just(false)
                coEvery { verificationCodeRepository.saveCode(any(), any(), any()) } returns true
                coEvery { emailService.sendVerificationCode(any(), any()) } just Runs

                emailVerificationService.sendCodeForSignUp(email)

                Then("인증 코드가 저장되고 이메일이 발송된다") {
                    coVerify(exactly = 1) { verificationCodeRepository.saveCode(email, any(), any()) }
                    coVerify(exactly = 1) { emailService.sendVerificationCode(email, any()) }
                }
            }
            When("이미 가입된 이메일이면") {
                coEvery { memberRepository.existsByEmail(email) } returns Mono.just(true)
                Then("EmailDuplicateException 예외가 발생한다") {
                    shouldThrow<EmailDuplicateException> {
                        emailVerificationService.sendCodeForSignUp(email)
                    }
                }
            }
        }

        Given("인증 코드 확인 및 증표 발급(verifyCodeAndIssueProofToken) 시") {
            val email = "test@example.com"
            val code = "123456"
            val proofToken = "valid-proof-token"

            When("인증 코드가 일치하면") {
                coEvery { verificationCodeRepository.findCodeByEmail(email) } returns code
                coEvery { verificationCodeRepository.deleteByEmail(email) } returns true
                coEvery { signUpProofRepository.issueProof(email) } returns proofToken

                val result = emailVerificationService.verifyCodeAndIssueProofToken(email, code)

                Then("코드가 삭제되고, 인증 성공(true) 및 증표가 발급된다") {
                    result shouldBe CodeConfirmationResponse(isVerified = true, proofToken = proofToken)
                    coVerify(exactly = 1) { verificationCodeRepository.deleteByEmail(email) }
                    coVerify(exactly = 1) { signUpProofRepository.issueProof(email) }
                }
            }
            When("인증 코드가 일치하지 않으면") {
                coEvery { verificationCodeRepository.findCodeByEmail(email) } returns "wrong-code"
                val result = emailVerificationService.verifyCodeAndIssueProofToken(email, code)
                Then("인증 실패(false) 및 증표는 발급되지 않는다") {
                    result shouldBe CodeConfirmationResponse(isVerified = false, proofToken = null)
                    coVerify(exactly = 0) { verificationCodeRepository.deleteByEmail(any()) }
                    coVerify(exactly = 0) { signUpProofRepository.issueProof(any()) }
                }
            }
        }
    }
}