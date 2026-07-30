package com.example.solo_play_web_server.place.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.string.shouldContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.springframework.core.codec.DecodingException
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.test.StepVerifier
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class KakaoApiServiceSpec : BehaviorSpec() {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var kakaoApiService: KakaoApiService

    init {
        beforeTest {
            mockWebServer = MockWebServer()
            mockWebServer.start()

            val webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build()

            kakaoApiService = KakaoApiService(webClient, "TEST_API_KEY")
        }

        afterTest {
            mockWebServer.shutdown()
        }

        Given("장소 검색 요청 시") {
            val keyword = "연남동 카페"
            val page = 1

            When("page 파라미터를 포함하여 요청하면") {
                Then("query, page, size와 Authorization 헤더를 URL encoding하여 전송한다") {
                    mockWebServer.enqueue(jsonResponse(successResponseJson()))

                    val responseMono = kakaoApiService.searchPlacesByKeyword(keyword, page)

                    StepVerifier.create(responseMono)
                        .assertNext { response ->
                            response.documents[0].placeName shouldBe "테스트 카페"
                        }
                        .verifyComplete()

                    val recordedRequest = mockWebServer.takeRequest()
                    val encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8).replace("+", "%20")
                    recordedRequest.requestUrl?.encodedPath shouldBe "/v2/local/search/keyword.json"
                    recordedRequest.path shouldContain "query=$encodedKeyword"
                    recordedRequest.path shouldContain "page=$page"
                    recordedRequest.path shouldContain "size=15"
                    recordedRequest.requestUrl?.queryParameter("query") shouldBe keyword
                    recordedRequest.requestUrl?.queryParameter("page") shouldBe page.toString()
                    recordedRequest.requestUrl?.queryParameter("size") shouldBe "15"
                    recordedRequest.getHeader("Authorization") shouldBe "KakaoAK TEST_API_KEY"
                }
            }

            When("page 파라미터 없이 키워드로만 검색하면") {
                Then("기본값인 page=1로 요청을 보내야 한다") {
                    mockWebServer.enqueue(jsonResponse(successResponseJson()))

                    val responseMono = kakaoApiService.searchPlacesByKeyword(keyword)

                    StepVerifier.create(responseMono)
                        .expectNextCount(1)
                        .verifyComplete()

                    val recordedRequest = mockWebServer.takeRequest()
                    recordedRequest.path shouldContain "page=1"
                    recordedRequest.getHeader("Authorization") shouldBe "KakaoAK TEST_API_KEY"
                }
            }

            When("카카오 API가 빈 documents를 반환하면") {
                Then("빈 목록 응답을 정상적으로 역직렬화한다") {
                    mockWebServer.enqueue(
                        jsonResponse("""{"documents":[],"meta":{"is_end":true,"total_count":0}}""")
                    )

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .assertNext { response ->
                            response.documents shouldBe emptyList()
                            response.meta.totalCount shouldBe 0
                        }
                        .verifyComplete()
                }
            }

            When("카카오 API가 400 에러를 반환하면") {
                Then("IllegalArgumentException을 발생시켜야 한다") {
                    val errorBody = """{"errorType":"MissingParameter","message":"query parameter required"}"""
                    mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody(errorBody))

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .expectErrorSatisfies { error ->
                            error.javaClass shouldBe IllegalArgumentException::class.java
                            error.message shouldContain "잘못된 요청입니다: $errorBody"
                        }
                        .verify()
                }
            }

            When("카카오 API가 401 Unauthorized를 반환하면") {
                Then("WebClient의 Unauthorized 예외 계약을 유지한다") {
                    mockWebServer.enqueue(MockResponse().setResponseCode(401))

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .expectError(WebClientResponseException.Unauthorized::class.java)
                        .verify()
                }
            }

            When("카카오 API가 429 Too Many Requests를 반환하면") {
                Then("WebClient의 TooManyRequests 예외 계약을 유지한다") {
                    mockWebServer.enqueue(MockResponse().setResponseCode(429))

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .expectError(WebClientResponseException.TooManyRequests::class.java)
                        .verify()
                }
            }

            When("카카오 API가 500 Internal Server Error를 반환하면") {
                Then("RuntimeException을 발생시켜야 한다") {
                    mockWebServer.enqueue(MockResponse().setResponseCode(500))

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .expectErrorSatisfies { error ->
                            error.javaClass shouldBe RuntimeException::class.java
                            error.message shouldBe "카카오 API 서버에 문제가 발생했습니다."
                        }
                        .verify()
                }
            }

            When("카카오 API가 503 Service Unavailable을 반환하면") {
                Then("WebClient의 ServiceUnavailable 예외 계약을 유지한다") {
                    mockWebServer.enqueue(MockResponse().setResponseCode(503))

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .expectError(WebClientResponseException.ServiceUnavailable::class.java)
                        .verify()
                }
            }

            When("카카오 API가 잘못된 JSON을 반환하면") {
                Then("DecodingException을 발생시킨다") {
                    mockWebServer.enqueue(jsonResponse("""{"documents":[}"""))

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .expectErrorSatisfies { error ->
                            error.shouldBeInstanceOf<DecodingException>()
                        }
                        .verify()
                }
            }

            When("카카오 API 연결이 응답 전에 종료되면") {
                Then("WebClientRequestException을 발생시킨다") {
                    repeat(2) {
                        mockWebServer.enqueue(
                            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                        )
                    }

                    StepVerifier.create(kakaoApiService.searchPlacesByKeyword(keyword, page))
                        .expectErrorSatisfies { error ->
                            error.shouldBeInstanceOf<WebClientRequestException>()
                        }
                        .verify()
                }
            }
        }
    }

    private fun jsonResponse(body: String): MockResponse {
        return MockResponse()
            .setBody(body)
            .addHeader("Content-Type", "application/json")
    }

    private fun successResponseJson(): String {
        return """
            {
              "documents": [{
                "id": "1",
                "place_name": "테스트 카페",
                "category_name": "음식점 > 카페",
                "address_name": "서울",
                "road_address_name": "서울",
                "place_url": "http://localhost/places/1"
              }],
              "meta": { "is_end": true, "total_count": 1 }
            }
        """.trimIndent()
    }
}
