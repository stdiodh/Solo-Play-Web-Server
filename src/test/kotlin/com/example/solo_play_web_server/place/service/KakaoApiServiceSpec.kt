package com.example.solo_play_web_server.place.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import java.net.URLEncoder

class KakaoApiServiceSpec : BehaviorSpec() {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var kakaoApiService: KakaoApiService

    init {
        beforeSpec {
            mockWebServer = MockWebServer()
            mockWebServer.start()

            val webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build()

            kakaoApiService = KakaoApiService(webClient).apply {
                val field = this::class.java.getDeclaredField("kakaoApiKey")
                field.isAccessible = true
                field.set(this, "TEST_API_KEY")
            }
        }

        afterSpec {
            mockWebServer.shutdown()
        }

        Given("장소 검색 요청 시") {
            val keyword = "연남동 카페"
            val page = 1

            When("카카오 API가 성공적으로 응답하면") {
                val successResponseJson = """
                {
                  "documents": [{
                    "id": "1", "place_name": "테스트 카페", "category_name": "음식점 > 카페",
                    "address_name": "서울", "road_address_name": "서울", "place_url": "http://place.map.kakao.com/1"
                  }],
                  "meta": { "is_end": true, "total_count": 1 }
                }
                """.trimIndent()
                mockWebServer.enqueue(
                    MockResponse().setBody(successResponseJson).addHeader("Content-Type", "application/json")
                )

                val responseMono = kakaoApiService.searchPlacesByKeyword(keyword, page)

                Then("미리 정의한 가짜 데이터를 반환하고, 올바른 요청을 보내야 한다") {
                    StepVerifier.create(responseMono)
                        .assertNext { response ->
                            response.documents.size shouldBe 1
                            response.documents[0].placeName shouldBe "테스트 카페"
                        }
                        .verifyComplete()

                    val recordedRequest = mockWebServer.takeRequest()

                    val encodedKeyword = URLEncoder.encode(keyword, "UTF-8").replace("+", "%20")

                    recordedRequest.path shouldContain "query=$encodedKeyword"
                    recordedRequest.path shouldContain "page=$page"
                    recordedRequest.getHeader("Authorization") shouldBe "KakaoAK TEST_API_KEY"
                }
            }

            When("카카오 API가 400 Bad Request 에러를 반환하면") {
                val errorBody = "{\"errorType\":\"MissingParameter\",\"message\":\"query parameter required\"}"
                mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody(errorBody))

                val responseMono = kakaoApiService.searchPlacesByKeyword(keyword, page)

                Then("IllegalArgumentException을 발생시켜야 한다") {
                    StepVerifier.create(responseMono)
                        .expectError(IllegalArgumentException::class.java)
                        .verify()
                }
            }

            When("카카오 API가 500 Internal Server Error를 반환하면") {
                mockWebServer.enqueue(MockResponse().setResponseCode(500))

                val responseMono = kakaoApiService.searchPlacesByKeyword(keyword, page)

                Then("RuntimeException을 발생시켜야 한다") {
                    StepVerifier.create(responseMono)
                        .expectError(RuntimeException::class.java)
                        .verify()
                }
            }
        }
    }
}