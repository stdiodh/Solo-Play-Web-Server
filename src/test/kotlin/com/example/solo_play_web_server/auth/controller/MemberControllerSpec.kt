package com.example.solo_play_web_server.auth.controller

import com.example.solo_play_web_server.auth.dto.*
import com.example.solo_play_web_server.auth.service.EmailVerificationService
import com.example.solo_play_web_server.auth.service.MemberService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import io.mockk.just
import io.mockk.runs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MemberControllerSpec(
    private val webTestClient: WebTestClient,

    @MockkBean
    private val memberService: MemberService,

    @MockkBean
    private val emailVerificationService: EmailVerificationService
) : BehaviorSpec({

    Given("이메일 중복 확인 API (GET /check-email-duplicate)") {
        When("사용 가능한 이메일로 요청하면") {
            val email = "available@example.com"
            coEvery { memberService.isEmailAlreadyExists(email) } returns false

            val response = webTestClient.get()
                .uri("/api/auth/check-email-duplicate?email={email}", email)
                .exchange()

            Then("200 OK와 함께 isAvailable: true를 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.data.isAvailable").isEqualTo(true)
            }
        }
        When("이미 사용 중인 이메일로 요청하면") {
            val email = "duplicate@example.com"
            coEvery { memberService.isEmailAlreadyExists(email) } returns true

            val response = webTestClient.get()
                .uri("/api/auth/check-email-duplicate?email={email}", email)
                .exchange()

            Then("409 Conflict와 함께 isAvailable: false를 반환한다") {
                response.expectStatus().is4xxClientError
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.data.isAvailable").isEqualTo(false)
            }
        }
    }

    Given("회원가입용 인증 코드 발송 API (POST /email-verify)") {
        When("정상적인 이메일로 요청하면") {
            val request = EmailVerificationRequest("test@example.com")
            coEvery { emailVerificationService.sendCodeForSignUp(request.email) } just runs

            val response = webTestClient.post()
                .uri("/api/auth/email-verify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("200 OK와 함께 성공 메시지를 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.message").isEqualTo("인증 코드를 발송했습니다. 이메일을 확인해주세요.")
            }
        }
    }

    Given("인증 코드 확인 및 증표 발급 API (POST /email-confirm)") {
        When("올바른 인증 코드로 요청하면") {
            val request = CodeConfirmationRequest("test@example.com", "123456")
            val proofToken = "valid-proof-token"
            coEvery { emailVerificationService.verifyCodeAndIssueProofToken(request.email, request.code) } returns
                    CodeConfirmationResponse(isVerified = true, proofToken = proofToken)

            val response = webTestClient.post()
                .uri("/api/auth/email-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("200 OK와 함께 isVerified: true와 proofToken을 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.data.isVerified").isEqualTo(true)
                    .jsonPath("$.data.proofToken").isEqualTo(proofToken)
            }
        }
    }

    Given("회원가입 API (POST /signup)") {
        When("유효한 정보로 요청하면") {
            val request = SignUpRequest(
                email = "test@example.com", password = "Password123!",
                agreement = Agreement(true, true, false, false),
                proofToken = "valid-proof-token"
            )
            coEvery { memberService.signUp(request) } just runs

            val response = webTestClient.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("201 Created와 함께 성공 메시지를 반환한다") {
                response.expectStatus().isCreated
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.message").isEqualTo("회원가입이 완료되었습니다.")
            }
        }
    }

    Given("로그인 API (POST /login)") {
        When("올바른 정보로 요청하면") {
            val request = LoginRequest("test@example.com", "Password123!")
            val tokenResponse = TokenResponse("Bearer",
                "accessToken", "refreshToken")
            coEvery { memberService.login(request) } returns tokenResponse

            val response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("200 OK와 함께 토큰 정보를 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.data.accessToken").isEqualTo("accessToken")
            }
        }
    }
})