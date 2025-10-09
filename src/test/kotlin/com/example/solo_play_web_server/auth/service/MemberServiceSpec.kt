package com.example.solo_play_web_server.auth.service

import com.example.solo_play_web_server.auth.dto.*
import com.example.solo_play_web_server.auth.entity.Member
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import com.example.solo_play_web_server.auth.repository.MemberRepository
import com.example.solo_play_web_server.auth.repository.RefreshTokenRepository
import com.example.solo_play_web_server.auth.repository.SignUpProofRepository
import com.example.solo_play_web_server.common.auth.JwtProvider
import com.example.solo_play_web_server.common.exception.LoginFailedException
import com.example.solo_play_web_server.common.exception.SignUpProofException
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
    lateinit var jwtProvider: JwtProvider
    @MockK
    lateinit var signUpProofRepository: SignUpProofRepository
    @MockK
    lateinit var refreshTokenRepository: RefreshTokenRepository

    @InjectMockKs
    lateinit var memberService: MemberService

    init {
        beforeTest { MockKAnnotations.init(this) }
        afterTest { clearAllMocks() }

        Given("최종 회원가입(signUp) 시") {
            val request = SignUpRequest(
                email = "test@example.com",
                password = "password123!",
                agreement = Agreement(true, true, true, true),
                proofToken = "valid-proof-token"
            )
            val savedMember = Member(
                id = "id", email = request.email, password = "encodedPassword",
                agreement = request.agreement, provider = AuthProvider.LOCAL, role = setOf(MemberRole.USER)
            )

            When("모든 정보가 유효하면") {
                coEvery { signUpProofRepository.consumeProof(request.proofToken) } returns request.email
                coEvery { memberRepository.existsByEmail(request.email) } returns Mono.just(false)
                every { passwordEncoder.encode(request.password) } returns "encodedPassword"
                coEvery { memberRepository.save(any()) } returns Mono.just(savedMember)

                // 실행
                memberService.signUp(request)

                Then("회원가입이 완료된다") {
                    val memberSlot = slot<Member>()
                    coVerify(exactly = 1) { memberRepository.save(capture(memberSlot)) }
                    val capturedMember = memberSlot.captured
                    capturedMember.email shouldBe request.email
                    capturedMember.password shouldBe "encodedPassword"
                }
            }

            When("인증 증표가 유효하지 않으면") {
                coEvery { signUpProofRepository.consumeProof(request.proofToken) } returns null
                Then("SignUpProofException 예외가 발생한다") {
                    val exception = shouldThrow<SignUpProofException> {
                        memberService.signUp(request)
                    }
                    exception.message shouldBe ("유효하지 않은 회원가입 요청입니다.")
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

                coEvery { refreshTokenRepository.save(any(), any()) } just runs

                val result = memberService.login(loginRequest)

                Then("성공적으로 로그인이 되고 토큰이 발급되며, Refresh Token이 저장된다"){
                    result shouldBe tokenResponse

                    coVerify(exactly = 1) { refreshTokenRepository.save(member.id!!, tokenResponse.refreshToken) }
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

        Given("로그아웃(logout) 시") {
            val userId = "user-id-123"

            When("사용자 ID로 로그아웃을 요청하면") {
                coEvery { refreshTokenRepository.deleteByUserId(userId) } returns true

                memberService.logout(userId)

                Then("해당 사용자의 Refresh Token이 삭제된다") {
                    coVerify(exactly = 1) { refreshTokenRepository.deleteByUserId(userId) }
                }
            }
        }
    }
}