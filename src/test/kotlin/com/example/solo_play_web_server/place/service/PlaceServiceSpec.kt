package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.dto.KakaoApiResponse
import com.example.solo_play_web_server.place.dto.Meta
import com.example.solo_play_web_server.place.entity.KakaoPlace
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.enum.MainCategory
import com.example.solo_play_web_server.place.enums.SubCategory
import com.example.solo_play_web_server.place.repository.PlaceRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class PlaceServiceSpec : BehaviorSpec() {

    @MockK
    lateinit var kakaoApiService: KakaoApiService
    @MockK
    lateinit var placeRepository: PlaceRepository
    @MockK
    lateinit var placeEnrichmentService: PlaceEnrichmentService

    @InjectMockKs
    lateinit var placeService: PlaceService

    init {
        beforeTest { MockKAnnotations.init(this) }
        afterTest { clearAllMocks() }

        Given("장소 검색 및 저장(searchAndSavePlacesByKeyword) 시") {
            val keyword = "카페"
            val page = 1

            val fakeKakaoPlace1 = KakaoPlace("1", "카페A", "음식점 > 카페", "서울 마포구", "...", "url1")
            val fakeKakaoPlace2 = KakaoPlace("2", "카페B", "음식점 > 카페 > 북카페", "서울 서대문구", "...", "url2")
            val fakeApiResponse = KakaoApiResponse(listOf(fakeKakaoPlace1, fakeKakaoPlace2), Meta(false, 2))

            val placesToSave = listOf(
                Place(kakaoPlaceId = "1", placeName = "카페A", area = "마포구", address = "...", description = "...", mainCategory = MainCategory.RELAX_LEISURE, subCategory = SubCategory.CAFE, level = Level.ONE, kakaoCategoryName = "...", displayTitle = "...", urls = listOf("url1")),
                Place(kakaoPlaceId = "2", placeName = "카페B", area = "서대문구", address = "...", description = "...", mainCategory = MainCategory.RELAX_LEISURE, subCategory = SubCategory.CAFE, level = Level.ONE, kakaoCategoryName = "...", displayTitle = "...", urls = listOf("url2"))
            )

            When("검색된 장소가 모두 새로운 장소일 경우") {
                coEvery { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(fakeApiResponse)
                coEvery { placeRepository.findByKakaoPlaceId(any()) } returns null // DB에 없다고 응답
                coEvery { placeRepository.saveAll(any<List<Place>>()) } returns Flux.fromIterable(placesToSave)
                coEvery { placeEnrichmentService.enrichPlaceData(any()) } just Runs

                val savedCount = placeService.searchAndSavePlacesByKeyword(keyword, page)

                Then("검색된 장소의 수만큼 저장되고, 각 장소에 대해 데이터 보강 작업이 호출된다") {
                    savedCount shouldBe 2
                    coVerify(exactly = 2) { placeRepository.findByKakaoPlaceId(any()) }
                    coVerify(exactly = 1) { placeRepository.saveAll(any<List<Place>>()) }
                    coVerify(exactly = 2) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("검색된 장소 중 일부가 이미 DB에 존재할 경우") {
                coEvery { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(fakeApiResponse)
                coEvery { placeRepository.findByKakaoPlaceId("1") } returns mockk<Place>() // 카페A는 이미 존재
                coEvery { placeRepository.findByKakaoPlaceId("2") } returns null // 카페B는 신규
                coEvery { placeRepository.saveAll(any<List<Place>>()) } returns Flux.fromIterable(listOf(placesToSave[1]))
                coEvery { placeEnrichmentService.enrichPlaceData(any()) } just Runs

                val savedCount = placeService.searchAndSavePlacesByKeyword(keyword, page)

                Then("새로운 장소 1개만 저장되고, 1개에 대해서만 데이터 보강 작업이 호출된다") {
                    savedCount shouldBe 1
                    coVerify(exactly = 1) { placeRepository.saveAll(any<List<Place>>()) }
                    coVerify(exactly = 1) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }
        }

        Given("레벨별 장소 추천(getRecommendedPlacesByLevel) 시") {
            val level = Level.ONE
            val fakePlacesFromDb = listOf(
                Place(id="id1", kakaoPlaceId = "1", placeName = "추천카페A", area = "마포구", address = "...", description = "...", mainCategory = MainCategory.RELAX_LEISURE, subCategory = SubCategory.CAFE, level = level, kakaoCategoryName = "...", displayTitle = "AI 제목1", displayTags = listOf("#태그1"), urls = listOf("url1")),
                Place(id="id2", kakaoPlaceId = "2", placeName = "추천카페B", area = "서대문구", address = "...", description = "...", mainCategory = MainCategory.RELAX_LEISURE, subCategory = SubCategory.CAFE, level = level, kakaoCategoryName = "...", displayTitle = "AI 제목2", displayTags = listOf("#태그2"), urls = listOf("url2"))
            )

            When("해당 레벨의 장소가 존재하면") {
                coEvery { placeRepository.findRandom10ByLevel(level) } returns fakePlacesFromDb

                val result = placeService.getRecommendedPlacesByLevel(level)

                Then("DB에서 조회된 장소 목록이 DTO로 변환되어 반환된다") {
                    result.size shouldBe 2
                    result[0].placeName shouldBe "추천카페A"
                    result[0].displayTitle shouldBe "AI 제목1"
                    result[1].area shouldBe "서대문구"
                    result[1].displayTags shouldBe listOf("#태그2")
                }
            }
        }
    }
}