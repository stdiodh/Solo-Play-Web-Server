package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.enum.MainCategory
import com.example.solo_play_web_server.place.enums.SubCategory
import com.example.solo_play_web_server.place.repository.PlaceRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class PlaceEnrichmentServiceSpec : BehaviorSpec() {

    private val placeRepository = mockk<PlaceRepository>()
    private val objectMapper = jacksonObjectMapper()
    private lateinit var webClient: WebClient
    private lateinit var mockWebServer: MockWebServer

    init {
        beforeTest {
            mockWebServer = MockWebServer()
            mockWebServer.start()
            webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build()
        }
        afterTest {
            mockWebServer.shutdown()
            clearMocks(placeRepository)
        }

        Given("장소 정보 보강(enrichPlaceData) 요청 시") {
            When("Gemini API가 일반 JSON으로 성공적으로 응답하면") {
                Then("장소의 displayTitle과 displayTags가 업데이트되고 DB에 저장된다") {
                    runTest {
                        val place = place()
                        val enrichedJson = """
                            {
                              "displayTitle": "성공적인 테스트 카페",
                              "displayTags": ["#성공", "#테스트", "#카페"]
                            }
                        """.trimIndent()
                        mockWebServer.enqueue(jsonResponse(geminiResponse(enrichedJson)))
                        every { placeRepository.save(place) } returns Mono.just(place)
                        val service = PlaceEnrichmentService(placeRepository, webClient, this)

                        service.enrichPlaceData(place).join()

                        place.displayTitle shouldBe "성공적인 테스트 카페"
                        place.displayTags shouldBe listOf("#성공", "#테스트", "#카페")
                        verify(exactly = 1) { placeRepository.save(place) }

                        val requestBody = mockWebServer.takeRequest().body.readUtf8()
                        requestBody shouldContain "테스트 카페"
                        requestBody shouldContain "음식점 > 카페"
                        requestBody shouldContain "서울 마포구"
                        requestBody shouldContain "15자 내외"
                        requestBody shouldContain "3~5개"
                        requestBody shouldContain "#"
                    }
                }
            }

            When("Gemini API가 json fence를 포함해 성공적으로 응답하면") {
                Then("fence를 제거하고 장소를 한 번 저장한다") {
                    runTest {
                        val place = place()
                        val enrichedJson = """
                            {
                              "displayTitle": "fence를 제거한 소개",
                              "displayTags": ["#한적함", "#혼자", "#카페"]
                            }
                        """.trimIndent()
                        mockWebServer.enqueue(
                            jsonResponse(geminiResponse("```json\n$enrichedJson\n```"))
                        )
                        every { placeRepository.save(place) } returns Mono.just(place)
                        val service = PlaceEnrichmentService(placeRepository, webClient, this)

                        service.enrichPlaceData(place).join()

                        place.displayTitle shouldBe "fence를 제거한 소개"
                        place.displayTags shouldBe listOf("#한적함", "#혼자", "#카페")
                        verify(exactly = 1) { placeRepository.save(place) }
                    }
                }
            }

            When("Gemini part의 JSON이 잘못되었을 경우") {
                Then("장소를 변경하거나 저장하지 않는다") {
                    runTest {
                        val place = place(displayTitle = "원본 소개", displayTags = listOf("#원본"))
                        mockWebServer.enqueue(jsonResponse(geminiResponse("""{"displayTitle":""")))
                        val service = PlaceEnrichmentService(placeRepository, webClient, this)

                        service.enrichPlaceData(place).join()

                        place.displayTitle shouldBe "원본 소개"
                        place.displayTags shouldBe listOf("#원본")
                        verify(exactly = 0) { placeRepository.save(any()) }
                    }
                }
            }

            When("Gemini 응답의 candidates 또는 parts가 비었을 경우") {
                Then("장소를 변경하거나 저장하지 않는다") {
                    runTest {
                        val noCandidatePlace = place(
                            kakaoPlaceId = "no-candidates",
                            displayTitle = "후보 없음 원본",
                            displayTags = listOf("#원본")
                        )
                        val noPartsPlace = place(
                            kakaoPlaceId = "no-parts",
                            displayTitle = "파트 없음 원본",
                            displayTags = listOf("#원본")
                        )
                        mockWebServer.enqueue(jsonResponse("""{"candidates":[]}"""))
                        mockWebServer.enqueue(
                            jsonResponse("""{"candidates":[{"content":{"parts":[]}}]}""")
                        )
                        val service = PlaceEnrichmentService(placeRepository, webClient, this)

                        service.enrichPlaceData(noCandidatePlace).join()
                        service.enrichPlaceData(noPartsPlace).join()

                        noCandidatePlace.displayTitle shouldBe "후보 없음 원본"
                        noCandidatePlace.displayTags shouldBe listOf("#원본")
                        noPartsPlace.displayTitle shouldBe "파트 없음 원본"
                        noPartsPlace.displayTags shouldBe listOf("#원본")
                        verify(exactly = 0) { placeRepository.save(any()) }
                    }
                }
            }

            When("Gemini API가 클라이언트 에러(400)를 반환하면") {
                Then("장소를 변경하거나 저장하지 않는다") {
                    runTest {
                        val place = place(displayTitle = "원본 소개", displayTags = listOf("#원본"))
                        mockWebServer.enqueue(MockResponse().setResponseCode(400))
                        val service = PlaceEnrichmentService(placeRepository, webClient, this)

                        service.enrichPlaceData(place).join()

                        place.displayTitle shouldBe "원본 소개"
                        place.displayTags shouldBe listOf("#원본")
                        verify(exactly = 0) { placeRepository.save(any()) }
                    }
                }
            }

            When("Gemini API가 서버 에러(500)를 반환하면") {
                Then("DB에 저장되지 않는다") {
                    runTest {
                        val place = place(displayTitle = "원본 소개", displayTags = listOf("#원본"))
                        mockWebServer.enqueue(MockResponse().setResponseCode(500))
                        val service = PlaceEnrichmentService(placeRepository, webClient, this)

                        service.enrichPlaceData(place).join()

                        place.displayTitle shouldBe "원본 소개"
                        place.displayTags shouldBe listOf("#원본")
                        verify(exactly = 0) { placeRepository.save(any()) }
                    }
                }
            }
        }
    }

    private fun jsonResponse(body: String): MockResponse {
        return MockResponse()
            .setBody(body)
            .addHeader("Content-Type", "application/json")
    }

    private fun geminiResponse(text: String): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "candidates" to listOf(
                    mapOf(
                        "content" to mapOf(
                            "parts" to listOf(mapOf("text" to text))
                        )
                    )
                )
            )
        )
    }

    private fun place(
        kakaoPlaceId: String = "kakao-123",
        displayTitle: String = "",
        displayTags: List<String?> = emptyList()
    ): Place {
        return Place(
            id = "place-$kakaoPlaceId",
            kakaoPlaceId = kakaoPlaceId,
            placeName = "테스트 카페",
            kakaoCategoryName = "음식점 > 카페",
            address = "서울 마포구",
            area = "마포구",
            description = "설명",
            mainCategory = MainCategory.RELAX_LEISURE,
            displayTitle = displayTitle,
            displayTags = displayTags,
            subCategory = SubCategory.CAFE,
            level = Level.ONE
        )
    }
}
