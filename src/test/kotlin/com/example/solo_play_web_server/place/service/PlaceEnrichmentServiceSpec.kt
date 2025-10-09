package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.enum.MainCategory
import com.example.solo_play_web_server.place.enums.SubCategory
import com.example.solo_play_web_server.place.repository.PlaceRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class PlaceEnrichmentServiceSpec : BehaviorSpec() {

    @MockK
    lateinit var placeRepository: PlaceRepository
    private lateinit var webClient: WebClient
    private lateinit var placeEnrichmentService: PlaceEnrichmentService
    private lateinit var mockWebServer: MockWebServer

    init {
        beforeSpec {
            MockKAnnotations.init(this)
            mockWebServer = MockWebServer()
            mockWebServer.start()
            webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build()
            placeEnrichmentService = PlaceEnrichmentService(placeRepository, webClient)
        }
        afterSpec { mockWebServer.shutdown() }
        afterTest { clearAllMocks() }

        Given("장소 정보 보강(enrichPlaceData) 요청 시") {
            val place = Place(
                id = "place-id-1", kakaoPlaceId = "kakao-123", placeName = "테스트 카페",
                kakaoCategoryName = "음식점 > 카페", address = "서울 마포구", area = "마포구",
                description = "설명", mainCategory = MainCategory.RELAX_LEISURE,
                displayTitle = "", displayTags = emptyList(),
                subCategory = SubCategory.CAFE, level = Level.ONE
            )

            When("Gemini API가 성공적으로 응답하면") { runTest {
                val successResponseJson = """
                {
                  "candidates": [{"content": {"parts": [{"text": "```json\n{\n  \"displayTitle\": \"성공적인 테스트 카페\",\n  \"displayTags\": [\"#성공\", \"#테스트\"]\n}\n```"}]}}]
                }
                """.trimIndent()
                mockWebServer.enqueue(MockResponse().setBody(successResponseJson).addHeader("Content-Type", "application/json"))

                coEvery { placeRepository.save(any()) } returns Mono.just(place)

                placeEnrichmentService.enrichPlaceData(place)

                Then("장소의 displayTitle과 displayTags가 업데이트되고 DB에 저장된다") {
                    val placeSlot = slot<Place>()
                    coVerify(timeout = 5000, exactly = 1) { placeRepository.save(capture(placeSlot)) }

                    val capturedPlace = placeSlot.captured
                    capturedPlace.displayTitle shouldBe "성공적인 테스트 카페"
                    capturedPlace.displayTags shouldBe listOf("#성공", "#테스트")
                }
            }}

            When("Gemini API가 서버 에러(500)를 반환하면") { runTest {
                mockWebServer.enqueue(MockResponse().setResponseCode(500))

                placeEnrichmentService.enrichPlaceData(place)

                Then("DB에 저장되지 않는다") {
                    coVerify(timeout = 5000, exactly = 0) { placeRepository.save(any()) }
                }
            }}
        }
    }
}