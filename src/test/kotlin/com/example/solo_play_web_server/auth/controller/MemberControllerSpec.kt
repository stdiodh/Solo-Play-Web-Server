package com.example.solo_play_web_server.auth.controller

import com.example.solo_play_web_server.auth.dto.*
import com.example.solo_play_web_server.auth.service.EmailVerificationService
import com.example.solo_play_web_server.auth.service.MemberService
import com.example.solo_play_web_server.common.auth.JwtAuthenticationFilter
import com.example.solo_play_web_server.common.auth.TokenProvider
import com.example.solo_play_web_server.common.config.SecurityConfig
import com.example.solo_play_web_server.common.exception.BusinessException
import com.example.solo_play_web_server.common.exception.EmailDuplicateException
import com.example.solo_play_web_server.common.exception.GlobalExceptionHandler
import com.example.solo_play_web_server.common.exception.LoginFailedException
import com.example.solo_play_web_server.common.exception.SignUpProofException
import com.example.solo_play_web_server.common.exception.VerificationCodeException
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.runs
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@WebFluxTest(controllers = [MemberController::class])
@Import(SecurityConfig::class, JwtAuthenticationFilter::class, GlobalExceptionHandler::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MemberControllerSpec(
    private val webTestClient: WebTestClient,

    @MockkBean
    private val memberService: MemberService,

    @MockkBean
    private val emailVerificationService: EmailVerificationService,

    @MockkBean
    private val tokenProvider: TokenProvider,

    @MockkBean(name = "mongoMappingContext")
    private val mongoMappingContext: MongoMappingContext
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
                response.expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.data.isAvailable").isEqualTo(false)
            }
        }
        When("일반 BusinessException이 발생하면") {
            val email = "business-error@example.com"
            coEvery {
                memberService.isEmailAlreadyExists(email)
            } throws BusinessException("잘못된 요청입니다.")

            val response = webTestClient.get()
                .uri("/api/auth/check-email-duplicate?email={email}", email)
                .exchange()

            Then("400 Bad Request와 예외 메시지를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("잘못된 요청입니다.")
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
        val email = "test@example.com"
        val code = "123456"
        val request = CodeConfirmationRequest(email, code)

        When("올바른 인증 코드로 요청하면") {
            val proofToken = "valid-proof-token"
            val successResponse = CodeConfirmationResponse(proofToken = proofToken)
            coEvery { emailVerificationService.verifyCodeAndIssueProofToken(request.email, request.code) } returns successResponse

            val response = webTestClient.post()
                .uri("/api/auth/email-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("200 OK와 함께 proofToken을 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.data.isVerified").doesNotExist()
                    .jsonPath("$.data.proofToken").isEqualTo(proofToken)
            }
        }

        When("잘못된 인증 코드로 요청하면") {
            coEvery {
                emailVerificationService.verifyCodeAndIssueProofToken(request.email, request.code)
            } throws VerificationCodeException("인증코드가 틀렸습니다.")

            // Act
            val response = webTestClient.post()
                .uri("/api/auth/email-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("401 UNAUTHORIZED와 함께 에러 메시지를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("인증코드가 틀렸습니다.")
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

        When("이메일, 비밀번호, 필수 약관과 가입 증표가 유효하지 않으면") {
            val request = SignUpRequest(
                email = "invalid-email",
                password = "weak",
                agreement = Agreement(false, false, false, false),
                proofToken = ""
            )

            val response = webTestClient.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("400과 필드별 validation 오류를 반환하고 회원가입을 호출하지 않는다") {
                response.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.data.email").isEqualTo("이메일 형식이 올바르지 않습니다.")
                    .jsonPath("$.data.password")
                    .isEqualTo("비밀번호는 8~20자의 영문 대/소문자, 숫자, 특수문자 조합이어야 합니다.")
                    .jsonPath("$['data']['agreement.isOver14']").isEqualTo("만 14세 이상이어야 합니다.")
                    .jsonPath("$['data']['agreement.isAgreedToTerms']")
                    .isEqualTo("서비스 이용 약관에 동의해야 합니다.")
                    .jsonPath("$.data.proofToken").isEqualTo("가입 증표는 필수 입력 항목입니다.")

                coVerify(exactly = 0) { memberService.signUp(request) }
            }
        }

        When("이메일과 비밀번호가 빈 값이면") {
            val request = SignUpRequest(
                email = "",
                password = "",
                agreement = Agreement(true, true, false, false),
                proofToken = "valid-proof-token-for-blank-fields"
            )

            val response = webTestClient.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("400과 빈 필드별 validation 오류를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.data.email").isEqualTo("이메일은 필수 입력 항목입니다.")
                    .jsonPath("$.data.password").exists()

                coVerify(exactly = 0) { memberService.signUp(request) }
            }
        }

        When("이미 가입된 이메일이면") {
            val request = SignUpRequest(
                email = "duplicate-signup@example.com",
                password = "Password123!",
                agreement = Agreement(true, true, false, false),
                proofToken = "duplicate-proof-token"
            )
            coEvery {
                memberService.signUp(request)
            } throws EmailDuplicateException("이미 가입된 이메일입니다.")

            val response = webTestClient.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("409 Conflict와 예외 메시지를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("이미 가입된 이메일입니다.")
            }
        }

        When("가입 증표가 유효하지 않으면") {
            val request = SignUpRequest(
                email = "invalid-proof@example.com",
                password = "Password123!",
                agreement = Agreement(true, true, false, false),
                proofToken = "invalid-proof-token"
            )
            coEvery {
                memberService.signUp(request)
            } throws SignUpProofException("유효하지 않은 회원가입 요청입니다.")

            val response = webTestClient.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("400 Bad Request와 예외 메시지를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("유효하지 않은 회원가입 요청입니다.")
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

        When("이메일 형식이 올바르지 않으면") {
            val request = LoginRequest("invalid-email", "Password123!")

            val response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("400과 email 필드 validation 오류를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.data.email").isEqualTo("이메일 형식이 올바르지 않습니다.")

                coVerify(exactly = 0) { memberService.login(request) }
            }
        }

        When("이메일과 비밀번호가 빈 값이면") {
            val request = LoginRequest("", "")

            val response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("400과 빈 필드별 validation 오류를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.data.email").isEqualTo("이메일은 필수 입력 항목입니다.")
                    .jsonPath("$.data.password").isEqualTo("비밀번호는 필수 입력 항목입니다.")

                coVerify(exactly = 0) { memberService.login(request) }
            }
        }

        When("인증 정보가 일치하지 않으면") {
            val request = LoginRequest("login-failed@example.com", "Password123!")
            coEvery {
                memberService.login(request)
            } throws LoginFailedException("사용자 이름 또는 비밀번호가 올바르지 않습니다.")

            val response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("401 Unauthorized와 예외 메시지를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("사용자 이름 또는 비밀번호가 올바르지 않습니다.")
            }
        }

        When("처리하지 못한 예외가 발생하면") {
            val request = LoginRequest("unexpected-error@example.com", "Password123!")
            coEvery {
                memberService.login(request)
            } throws RuntimeException("내부 구현 메시지")

            val response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()

            Then("500과 공통 오류 응답을 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("에러가 발생했습니다.")
            }
        }
    }

    Given("로그아웃 API (POST /logout)") {
        When("인증 없이 요청하면") {
            val response = webTestClient.post()
                .uri("/api/auth/logout")
                .exchange()

            Then("401 Unauthorized를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
            }
        }

        When("인증된 사용자가 요청하면") {
            val userId = "authenticated-user-id"
            coEvery { memberService.logout(userId) } just runs

            val response = webTestClient
                .mutateWith(mockUser(userId).roles("USER"))
                .post()
                .uri("/api/auth/logout")
                .exchange()

            Then("해당 사용자를 로그아웃하고 200 OK를 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")

                coVerify(exactly = 1) { memberService.logout(userId) }
            }
        }
    }
})
