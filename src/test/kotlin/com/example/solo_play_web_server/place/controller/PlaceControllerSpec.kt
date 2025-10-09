package com.example.solo_play_web_server.place.controller

import com.example.solo_play_web_server.place.dto.RecommendPlaceByLevelResponse
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.service.PlaceService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PlaceControllerSpec(
    private val webTestClient: WebTestClient,

    @MockkBean
    private val placeService: PlaceService
) : BehaviorSpec({
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
    }
})