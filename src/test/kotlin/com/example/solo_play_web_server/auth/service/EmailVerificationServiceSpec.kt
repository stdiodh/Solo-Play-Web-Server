package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.CodeConfirmationResponse
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.SignUpProofRepository
import com.example.solo_play_web_server.auth.repository.VerificationCodeRepository
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.VerificationCodeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import reactor.core.publisher.Mono
import java.time.Duration

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

                Then("6자리 인증 코드가 10분 TTL로 저장되고 같은 코드가 이메일로 발송된다") {
                    val codeSlot = slot<String>()
                    coVerify(exactly = 1) {
                        verificationCodeRepository.saveCode(
                            email,
                            capture(codeSlot),
                            Duration.ofMinutes(10)
                        )
                    }
                    codeSlot.captured.matches(Regex("\\d{6}")) shouldBe true
                    coVerify(exactly = 1) {
                        emailService.sendVerificationCode(email, codeSlot.captured)
                    }
                }
            }
            When("이미 가입된 이메일이면") {
                coEvery { memberRepository.existsByEmail(email) } returns Mono.just(true)
                Then("EmailDuplicateException 예외가 발생한다") {
                    shouldThrow<EmailDuplicateException> {
                        emailVerificationService.sendCodeForSignUp(email)
                    }
                    coVerify(exactly = 0) { verificationCodeRepository.saveCode(any(), any(), any()) }
                    coVerify(exactly = 0) { emailService.sendVerificationCode(any(), any()) }
                }
            }
            When("인증 코드 저장 결과가 false이면") {
                coEvery { memberRepository.existsByEmail(email) } returns Mono.just(false)
                coEvery { verificationCodeRepository.saveCode(email, any(), Duration.ofMinutes(10)) } returns false

                Then("예외가 발생하고 이메일은 발송되지 않는다") {
                    shouldThrow<RuntimeException> {
                        emailVerificationService.sendCodeForSignUp(email)
                    }
                    coVerify(exactly = 0) { emailService.sendVerificationCode(any(), any()) }
                }
            }
        }

        Given("인증 코드 확인 및 증표 발급(verifyCodeAndIssueProofToken) 시") {
            val email = "test@example.com"
            val code = "123456"
            val proofToken = "valid-proof-token"

            When("저장된 인증 코드가 일치하면") {
                coEvery { verificationCodeRepository.findCodeByEmail(email) } returns code
                coEvery { verificationCodeRepository.deleteByEmail(email) } returns true
                coEvery { signUpProofRepository.issueProof(email) } returns proofToken

                val response = emailVerificationService.verifyCodeAndIssueProofToken(email, code)

                Then("인증 코드를 삭제하고 가입 증표를 한 번 발급한다") {
                    response shouldBe CodeConfirmationResponse(proofToken)
                    coVerifySequence {
                        verificationCodeRepository.findCodeByEmail(email)
                        verificationCodeRepository.deleteByEmail(email)
                        signUpProofRepository.issueProof(email)
                    }
                }
            }

            When("일치한 인증 코드 삭제에 실패하면") {
                coEvery { verificationCodeRepository.findCodeByEmail(email) } returns code
                coEvery { verificationCodeRepository.deleteByEmail(email) } returns false

                Then("VerificationCodeException이 발생하고 가입 증표를 발급하지 않는다") {
                    shouldThrow<VerificationCodeException> {
                        emailVerificationService.verifyCodeAndIssueProofToken(email, code)
                    }
                    coVerifySequence {
                        verificationCodeRepository.findCodeByEmail(email)
                        verificationCodeRepository.deleteByEmail(email)
                    }
                    coVerify(exactly = 0) { signUpProofRepository.issueProof(any()) }
                }
            }

            When("저장된 인증 코드가 없어 null을 반환하면") {
                coEvery { verificationCodeRepository.findCodeByEmail(email) } returns null

                Then("VerificationCodeException 예외가 발생한다") {
                    shouldThrow<VerificationCodeException> {
                        emailVerificationService.verifyCodeAndIssueProofToken(email, code)
                    }
                    coVerify(exactly = 0) { verificationCodeRepository.deleteByEmail(any()) }
                    coVerify(exactly = 0) { signUpProofRepository.issueProof(any()) }
                }
            }

            When("저장된 인증 코드가 일치하지 않으면") {
                coEvery { verificationCodeRepository.findCodeByEmail(email) } returns "wrong-code"

                Then("VerificationCodeException 예외가 발생한다") {
                    shouldThrow<VerificationCodeException> {
                        emailVerificationService.verifyCodeAndIssueProofToken(email, code)
                    }
                    coVerify(exactly = 0) { verificationCodeRepository.deleteByEmail(any()) }
                    coVerify(exactly = 0) { signUpProofRepository.issueProof(any()) }
                }
            }
        }
    }
}
