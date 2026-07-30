package com.example.solo_play_web_server.place.controller

import com.example.solo_play_web_server.common.auth.JwtAuthenticationFilter
import com.example.solo_play_web_server.common.auth.TokenProvider
import com.example.solo_play_web_server.common.config.SecurityConfig
import com.example.solo_play_web_server.common.exception.GlobalExceptionHandler
import com.example.solo_play_web_server.place.dto.RecommendPlaceByLevelResponse
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.service.PlaceService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [PlaceController::class])
@Import(SecurityConfig::class, JwtAuthenticationFilter::class, GlobalExceptionHandler::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PlaceControllerSpec(
    private val webTestClient: WebTestClient,

    @MockkBean
    private val placeService: PlaceService,

    @MockkBean
    private val tokenProvider: TokenProvider,

    @MockkBean(name = "mongoMappingContext")
    private val mongoMappingContext: MongoMappingContext
) : BehaviorSpec({
    Given("장소 수집 API (GET /api/places)") {
        When("현재 SecurityConfig 계약에 따라 USER 역할로 page 없이 요청하면 (Admin 문서와 충돌)") {
            val keyword = "혼자 가기 좋은 카페"
            coEvery {
                placeService.searchAndSavePlacesByKeyword(keyword, 1)
            } returns 2

            val response = webTestClient
                .mutateWith(mockUser().roles("USER"))
                .get()
                .uri { builder ->
                    builder.path("/api/places")
                        .queryParam("keyword", keyword)
                        .build()
                }
                .exchange()

            Then("기본 page 1로 수집하고 저장 건수를 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.data").isEqualTo(2)

                coVerify(exactly = 1) {
                    placeService.searchAndSavePlacesByKeyword(keyword, 1)
                }
            }
        }

        When("서비스에서 예외가 발생하면") {
            val keyword = "수집 오류"
            val page = 3
            coEvery {
                placeService.searchAndSavePlacesByKeyword(keyword, page)
            } throws RuntimeException("Kakao 조회 실패")

            val response = webTestClient
                .mutateWith(mockUser().roles("USER"))
                .get()
                .uri { builder ->
                    builder.path("/api/places")
                        .queryParam("keyword", keyword)
                        .queryParam("page", page)
                        .build()
                }
                .exchange()

            Then("500과 서비스 오류 메시지를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("Kakao 조회 실패")
            }
        }

        When("인증 없이 요청하면") {
            val keyword = "비인증 수집"

            val response = webTestClient.get()
                .uri { builder ->
                    builder.path("/api/places")
                        .queryParam("keyword", keyword)
                        .build()
                }
                .exchange()

            Then("401 Unauthorized로 차단한다") {
                response.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
                coVerify(exactly = 0) {
                    placeService.searchAndSavePlacesByKeyword(keyword, 1)
                }
            }
        }
    }

    Given("레벨별 추천 장소 API (GET /api/places/recommendations)") {
        val level = Level.ONE

        When("USER 역할로 유효한 레벨 파라미터로 요청하면") {
            val fakeResponse = listOf(
                RecommendPlaceByLevelResponse(
                    level = level, imageUrl = "url1", placeName = "테스트 추천 카페",
                    displayTitle = "AI가 만든 제목", area = "마포구", displayTags = listOf("#추천", "#카페")
                )
            )
            coEvery { placeService.getRecommendedPlacesByLevel(level) } returns fakeResponse

            val response = webTestClient
                .mutateWith(mockUser().roles("USER"))
                .get()
                .uri("/api/places/recommendations?level=ONE")
                .exchange()

            Then("200 OK와 함께 추천 장소 DTO 목록을 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.data").isArray
                    .jsonPath("$.data[0].placeName").isEqualTo("테스트 추천 카페")
            }
        }

        When("서비스 로직에서 예외가 발생하면") {
            coEvery { placeService.getRecommendedPlacesByLevel(level) } throws RuntimeException("DB 조회 실패")

            val response = webTestClient
                .mutateWith(mockUser().roles("USER"))
                .get()
                .uri("/api/places/recommendations?level=ONE")
                .exchange()

            Then("500 Internal Server Error를 반환한다") {
                response.expectStatus().is5xxServerError
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("DB 조회 실패")
            }
        }

        When("추천 결과가 비어 있으면") {
            coEvery {
                placeService.getRecommendedPlacesByLevel(Level.TWO)
            } returns emptyList()

            val response = webTestClient
                .mutateWith(mockUser().roles("USER"))
                .get()
                .uri("/api/places/recommendations?level=TWO")
                .exchange()

            Then("200 OK와 빈 목록을 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.data").isArray
                    .jsonPath("$.data").isEmpty
            }
        }

        When("지원하지 않는 level 값으로 요청하면") {
            val response = webTestClient
                .mutateWith(mockUser().roles("USER"))
                .get()
                .uri("/api/places/recommendations?level=UNKNOWN")
                .exchange()

            Then("400 Bad Request와 공통 입력 오류를 반환한다") {
                response.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("ERROR")
                    .jsonPath("$.message").isEqualTo("입력값이 올바르지 않습니다.")
            }
        }

        When("인증 없이 요청하면") {
            val response = webTestClient.get()
                .uri("/api/places/recommendations?level=THREE")
                .exchange()

            Then("401 Unauthorized로 차단한다") {
                response.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
            }
        }
    }
})
