package com.example.solo_play_web_server.place

import com.example.solo_play_web_server.auth.dto.Agreement
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
import com.example.solo_play_web_server.auth.service.EmailService
import com.example.solo_play_web_server.auth.service.MemberService
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.InvalidTokenException
import com.example.solo_play_web_server.common.exception.NicknameDuplicateException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.springframework.security.crypto.password.PasswordEncoder
import reactor.core.publisher.Mono
import java.time.Duration

class MemberServiceSpec : BehaviorSpec (){
    @MockK
    lateinit var memberRepository: MemberRepository
    @MockK
    lateinit var passwordEncoder: PasswordEncoder
    @MockK
    lateinit var pendingMemberRepository: PendingMemberRepository
    @MockK
    lateinit var emailService: EmailService
    @MockK
    lateinit var jwtProvider: JwtProvider
    @InjectMockKs
    lateinit var memberService: MemberService

    init {
        beforeSpec {
            MockKAnnotations.init(this)
        }

        afterTest {
            clearAllMocks()
        }

        Given("회원가입 요청(requestSignUp) 시") {
            val signUpRequest = SignUpRequest(
                email = "test@example.com",
                password = "password123",
                nickname = "tester",
                agreement = Agreement(true, true, true, true)
            )

            When("정상적인 정보로 요청하면") {
                coEvery { memberRepository.existsByEmail(any()) } returns Mono.just(false)
                coEvery { memberRepository.existsByNickname(any()) } returns Mono.just(false)
                every { passwordEncoder.encode(any()) } returns "encoderPassword"
                coEvery { pendingMemberRepository.save(any(), any(), any()) } returns true
                coEvery { emailService.sendVerificationCode(any(), any()) } returns Unit

                memberService.requestSignUp(signUpRequest)

                Then("임시 회원 정보가 저장되고 인증 메일이 발송된다.") {
                    coVerify(exactly = 1) { pendingMemberRepository.save(signUpRequest.email, any(), Duration.ofMinutes(10)) }
                    coVerify(exactly = 1) { emailService.sendVerificationCode(signUpRequest.email, any()) }
                }
            }

            When("이미 사용 중인 이메일로 요청하면"){
                coEvery { memberRepository.existsByEmail(signUpRequest.email) } returns Mono.just(true)

                Then("EmailDuplicateException 예외가 발생한다.") {
                    shouldThrow<EmailDuplicateException>{
                        memberService.requestSignUp(signUpRequest)
                    }
                }
            }

            When("이미 사용 중인 닉네임으로 요청하면"){
                coEvery { memberRepository.existsByEmail(any()) } returns Mono.just(false)
                coEvery { memberRepository.existsByNickname(any()) } returns Mono.just(true)

                Then("NickNameDuplicateException 예외가 발생한다."){
                    shouldThrow<NicknameDuplicateException> {
                        memberService.requestSignUp(signUpRequest)
                    }
                }
            }

            When("임시 회원 정보 저장에 실패하면") {
                coEvery { memberRepository.existsByEmail(any()) } returns Mono.just(false)
                coEvery { memberRepository.existsByNickname(any()) } returns Mono.just(false)
                every { passwordEncoder.encode(any()) } returns "encodedPassword"
                coEvery { pendingMemberRepository.save(any(), any(), any()) } returns false

                Then("RuntimeException 예외가 발생하고 이메일은 발송되지 않는다") {
                    val exception = shouldThrow<RuntimeException> {
                        memberService.requestSignUp(signUpRequest)
                    }
                    exception.message shouldBe "임시 회원 저장에 실패했습니다."

                    coVerify(exactly = 0) { emailService.sendVerificationCode(any(), any()) }
                }
            }
        }

        Given("인증 및 회원가입(verifyCodeAndSignUp) 시"){
            val request = SendVerifyEmailRequest("test@example.com", "123456")
            val pendingMember = PendingMember("test@example.com", "encodedPassword", "tester", Agreement(true, true, true, true))
            val verificationData = VerificationData(pendingMember, "123456")
            val savedMember = Member("id","test@example.com", "encodedPassword", "tester",
                null, AuthProvider.LOCAL, setOf(MemberRole.USER),
                Agreement(true, true, true, true), true)
            val tokenResponse = TokenResponse("Bearer","accessToken", "refreshToken")


            When("올바른 인증 코드로 요청하면"){
                coEvery { pendingMemberRepository.findByEmail(request.email) } returns verificationData
                coEvery { pendingMemberRepository.deleteByEmail(request.email) } returns Unit
                coEvery { memberRepository.save(any()) } returns Mono.just(savedMember)
                every { jwtProvider.generateTokens(savedMember.id!!, savedMember.role) } returns tokenResponse

                val result = memberService.verifyCodeAndSignUp(request)

                Then("회원가입이 완료되고 토큰이 발급된다."){
                    result shouldBe tokenResponse
                    coVerify(exactly = 1) { memberRepository.save(any()) }
                    coVerify(exactly = 1) { pendingMemberRepository.deleteByEmail(request.email) }
                }
            }

            When("인증 코드가 일치하지 않으면"){
                val wrongVerificationData = verificationData.copy(code = "654321")
                coEvery { pendingMemberRepository.findByEmail(request.email) } returns wrongVerificationData

                Then("InvalidTokenException 예외가 발생한다."){
                    shouldThrow<InvalidTokenException> {
                        memberService.verifyCodeAndSignUp(request)
                    }
                }
            }

            When("인증 코드의 유효시간이 지나면"){
                coEvery { pendingMemberRepository.findByEmail(request.email) } returns null

                Then("InvalidTokenException 예외가 발생한다") {
                    shouldThrow<InvalidTokenException> {
                        memberService.verifyCodeAndSignUp(request)
                    }
                }
            }
        }

        Given("로그인(login) 시"){
            val loginRequest = LoginRequest("test@example.com", "password123")
            val member = Member("id", "test@example.com", "encodedPassword", "tester",
                null, AuthProvider.LOCAL, setOf(MemberRole.USER),
                Agreement(true, true, true, true), true)
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

                Then("IllegalArgumentException 예외가 발생한다."){
                    shouldThrow<IllegalArgumentException> {
                        memberService.login(loginRequest)
                    }
                }
            }

            When("비밀번호가 일치하지 않으면"){
                coEvery { memberRepository.findByEmail(loginRequest.email) } returns Mono.just(member)
                every { passwordEncoder.matches(loginRequest.password, member.password) } returns false

                Then("IllegalArgumentException 예외가 발생한다."){
                    shouldThrow<IllegalArgumentException> {
                        memberService.login(loginRequest)
                    }
                }
            }
        }
    }


}