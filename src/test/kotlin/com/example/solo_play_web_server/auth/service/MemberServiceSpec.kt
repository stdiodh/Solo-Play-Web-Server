package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.*
import com.example.solo_play_web_server.auth.entity.Member
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.VerificationCodeRepository
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.InvalidTokenException
import com.example.solo_play_web_server.common.exception.LoginFailedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono

class MemberServiceSpec : BehaviorSpec() {
    @MockK
    lateinit var memberRepository: MemberRepository
    @MockK
    lateinit var passwordEncoder: PasswordEncoder
    @MockK
    lateinit var verificationCodeRepository: VerificationCodeRepository // 수정됨
    @MockK
    lateinit var emailService: EmailService
    @MockK
    lateinit var jwtProvider: JwtProvider
    @InjectMockKs
    lateinit var memberService: MemberService

    init {
        beforeTest {
            MockKAnnotations.init(this)
        }

        afterTest {
            clearAllMocks()
        }

        Given("이메일 중복 확인(isEmailAlreadyExists) 시") {
            val email = "test@example.com"
            When("이미 사용 중인 이메일이면") {
                coEvery { memberRepository.existsByEmail(email) } returns Mono.just(true)
                val result = memberService.isEmailAlreadyExists(email)
                Then("true를 반환한다") {
                    result shouldBe true
                }
            }
            When("사용 가능한 이메일이면") {
                coEvery { memberRepository.existsByEmail(email) } returns Mono.just(false)
                val result = memberService.isEmailAlreadyExists(email)
                Then("false를 반환한다") {
                    result shouldBe false
                }
            }
        }

        Given("인증 코드 발송(sendVerificationCode) 시") {
            val request = EmailVerificationRequest("test@example.com")
            When("정상적으로 요청하면") {
                coEvery { memberRepository.existsByEmail(any()) } returns Mono.just(false)
                coEvery { verificationCodeRepository.saveCode(any(), any(), any()) } returns true
                coEvery { emailService.sendVerificationCode(any(), any()) } returns Unit

                memberService.sendVerificationCode(request)

                Then("인증 코드가 저장되고 이메일이 발송된다") {
                    coVerify(exactly = 1) { verificationCodeRepository.saveCode(request.email, any(), any()) }
                    coVerify(exactly = 1) { emailService.sendVerificationCode(request.email, any()) }
                }
            }

            When("이미 가입된 이메일이면") {
                coEvery { memberRepository.existsByEmail(request.email) } returns Mono.just(true)
                Then("EmailDuplicateException 예외가 발생한다.") {
                    val exception = shouldThrow<EmailDuplicateException> {
                        memberService.sendVerificationCode(request)
                    }
                    exception.message shouldBe "이미 사용 중인 이메일입니다."
                }
            }

            When("임시 코드 저장에 실패하면") {
                coEvery { memberRepository.existsByEmail(any()) } returns Mono.just(false)
                coEvery { verificationCodeRepository.saveCode(any(), any(), any()) } returns false

                Then("RuntimeException 예외가 발생하고 이메일은 발송되지 않는다") {
                    val exception = shouldThrow<RuntimeException> {
                        memberService.sendVerificationCode(request)
                    }
                    exception.message shouldBe "인증 코드 저장에 실패했습니다."

                    coVerify(exactly = 0) { emailService.sendVerificationCode(any(), any()) }
                }
            }
        }

        Given("최종 회원가입(signUp) 시") {
            val request = SignUpRequest(
                email = "test@example.com", password = "password123!",
                code = "123456", agreement = Agreement(true, true, true, true)
            )
            val savedMember = Member(
                id = "id", email = request.email, password = "encodedPassword",
                provider = AuthProvider.LOCAL, role = setOf(MemberRole.USER), agreement = request.agreement
            )
            val tokenResponse = TokenResponse("Bearer", "accessToken", "refreshToken")

            When("올바른 정보로 요청하면") {
                coEvery { verificationCodeRepository.findCodeByEmail(request.email) } returns request.code
                coEvery { memberRepository.existsByEmail(request.email) } returns Mono.just(false)
                every { passwordEncoder.encode(request.password) } returns "encodedPassword"
                coEvery { memberRepository.save(any()) } returns Mono.just(savedMember)
                coEvery { verificationCodeRepository.deleteCodeByEmail(request.email) } returns Unit
                every { jwtProvider.generateTokens(savedMember.id!!, savedMember.role) } returns tokenResponse

                val result = memberService.signUp(request)

                Then("회원가입이 완료되고 토큰이 발급되며 코드는 삭제된다") {
                    result shouldBe tokenResponse
                    coVerify(exactly = 1) { memberRepository.save(any()) }
                    coVerify(exactly = 1) { verificationCodeRepository.deleteCodeByEmail(request.email) }
                }
            }

            When("인증 코드가 일치하지 않으면") {
                coEvery { verificationCodeRepository.findCodeByEmail(request.email) } returns "wrong-code"
                Then("InvalidTokenException 예외가 발생한다") {
                    val exception = shouldThrow<InvalidTokenException> {
                        memberService.signUp(request)
                    }
                    exception.message shouldBe "인증 코드가 일치하지 않습니다."
                }
            }

            When("인증 코드 시간이 만료되거나 유효하지 않으면"){
                coEvery { verificationCodeRepository.findCodeByEmail(request.email)} returns null

                Then("InvalidTokenException 예외가 발생한다"){
                    val exception = shouldThrow<InvalidTokenException> {
                        memberService.signUp(request)
                    }
                    exception.message shouldBe "인증 코드가 만료되었거나 유효하지 않습니다."
                }
            }
        }

        Given("로그인(login) 시"){
            val loginRequest = LoginRequest("test@example.com", "password123")
            val member = Member(
                "id", "test@example.com", "encodedPassword",
                null, AuthProvider.LOCAL, setOf(MemberRole.USER),
                Agreement(true, true, true, true)
            )
            val tokenResponse = TokenResponse("Bearer", "accessToken", "refreshToken")

            When("올바른 이메일과 비밀번호로 요청하면"){
                coEvery { memberRepository.findByEmail(loginRequest.email) } returns Mono.just(member)
                every { passwordEncoder.matches(loginRequest.password, member.password) } returns true
                every { jwtProvider.generateTokens(member.id!!, member.role) } returns tokenResponse

                val result = memberService.login(loginRequest)

                Then("성공적으로 로그인이되고 토큰이 발급된다."){
                    result shouldBe tokenResponse
                }
            }

            When("가입되지 않은 이메일로 요청하면"){
                coEvery { memberRepository.findByEmail(loginRequest.email) } returns Mono.empty()

                Then("LoginFailedException 예외가 발생한다."){
                    val exception = shouldThrow<LoginFailedException> {
                        memberService.login(loginRequest)
                    }
                    exception.message shouldBe "존재하지 않는 계정입니다. 회원가입 하시겠습니까?"
                }
            }

            When("비밀번호가 일치하지 않으면"){
                coEvery { memberRepository.findByEmail(loginRequest.email) } returns Mono.just(member)
                every { passwordEncoder.matches(loginRequest.password, member.password) } returns false

                Then("LoginFailedException 예외가 발생한다."){
                    val exception = shouldThrow<LoginFailedException> {
                        memberService.login(loginRequest)
                    }
                    exception.message shouldBe "사용자 이름 또는 비밀번호가 올바르지 않습니다."
                }
            }
        }
    }
}