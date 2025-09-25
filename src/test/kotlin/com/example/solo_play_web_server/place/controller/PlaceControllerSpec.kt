package com.example.solo_play_web_server.place.controller

import com.example.solo_play_web_server.place.dto.RecommendPlaceResponse
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.service.PlaceService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PlaceControllerSpec(
    private val webTestClient: WebTestClient,

    @MockkBean
    private val placeService: PlaceService
) : BehaviorSpec({

    Given("장소 검색 및 저장 API (GET /api/places)") {
        val keyword = "카페"
        val page = 1

        When("유효한 키워드로 요청이 들어오면") {
            coEvery { placeService.searchAndSavePlacesByKeyword(keyword, page) } returns 5

            val response = webTestClient.get()
                .uri("/api/places?keyword={keyword}&page={page}", keyword, page)
                .exchange()

            Then("200 OK와 함께 저장된 장소의 수를 반환한다") {
                response.expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("SUCCESS")
                    .jsonPath("$.message").isEqualTo("'$keyword' 검색 결과, 5 개의 새로운 장소를 저장했습니다.")
                    .jsonPath("$.data").isEqualTo(5)
            }
        }
    }

    Given("레벨별 추천 장소 API (GET /api/places/recommendations)") {
        When("유효한 레벨 파라미터(ONE)로 요청하면") {
            // Arrange
            val level = Level.ONE
            val fakeResponse = listOf(
                RecommendPlaceResponse(
                    level = Level.ONE, imageUrl = "url1", displayTitle = "AI가 만든 제목",
                    placeName = "테스트 추천 카페", area = "마포구", displayTags = listOf("#추천", "#카페")
                )
            )
            coEvery { placeService.getRecommendedPlacesByLevel(level) } returns fakeResponse

            val response = webTestClient.get()
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

        When("잘못된 레벨 파라미터로 요청하면") {
            val response = webTestClient.get()
                .uri("/api/places/recommendations?level=INVALID_LEVEL")
                .exchange()

            Then("500 internalServerError 에러를 반환한다") {
                response.expectStatus().is5xxServerError
            }
        }
    }
})