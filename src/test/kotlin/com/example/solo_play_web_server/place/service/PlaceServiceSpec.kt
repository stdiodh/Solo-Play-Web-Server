package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.dto.KakaoApiResponse
import com.example.solo_play_web_server.place.dto.Meta
import com.example.solo_play_web_server.place.entity.KakaoPlace
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.enum.MainCategory
import com.example.solo_play_web_server.place.enums.SubCategory
import com.example.solo_play_web_server.place.repository.PlaceRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Job
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class PlaceServiceSpec : BehaviorSpec() {

    private val kakaoApiService = mockk<KakaoApiService>()
    private val placeRepository = mockk<PlaceRepository>()
    private val placeEnrichmentService = mockk<PlaceEnrichmentService>()
    private val enrichmentJob = mockk<Job>(relaxed = true)
    private val placeService = PlaceService(kakaoApiService, placeRepository, placeEnrichmentService)

    init {
        afterTest {
            clearMocks(kakaoApiService, placeRepository, placeEnrichmentService)
        }

        Given("장소 검색 및 저장(searchAndSavePlacesByKeyword) 시") {
            val keyword = "카페"
            val page = 1

            When("검색된 장소가 모두 새로운 장소일 경우") {
                Then("검색된 장소의 수만큼 저장되고, 각 장소에 대해 데이터 보강 작업이 호출된다") {
                    val apiResponse = kakaoResponse(
                        kakaoPlace("1", "카페A", "음식점 > 카페", "서울 마포구"),
                        kakaoPlace("2", "카페B", "음식점 > 카페 > 북카페", "서울 서대문구")
                    )
                    val savedPlaces = slot<List<Place>>()
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(apiResponse)
                    coEvery { placeRepository.findByKakaoPlaceId(any()) } returns null
                    every { placeRepository.saveAll(capture(savedPlaces)) } answers {
                        Flux.fromIterable(savedPlaces.captured)
                    }
                    every { placeEnrichmentService.enrichPlaceData(any()) } returns enrichmentJob

                    val savedCount = placeService.searchAndSavePlacesByKeyword(keyword, page)

                    savedCount shouldBe 2
                    savedPlaces.captured.map { it.kakaoPlaceId } shouldBe listOf("1", "2")
                    savedPlaces.captured.map { it.area } shouldBe listOf("마포구", "서대문구")
                    coVerify(exactly = 2) { placeRepository.findByKakaoPlaceId(any()) }
                    verify(exactly = 1) { placeRepository.saveAll(any<List<Place>>()) }
                    verify(exactly = 2) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("검색된 장소 중 일부가 이미 DB에 존재할 경우") {
                Then("새로운 장소 1개만 저장되고, 1개에 대해서만 데이터 보강 작업이 호출된다") {
                    val apiResponse = kakaoResponse(
                        kakaoPlace("1", "카페A"),
                        kakaoPlace("2", "카페B")
                    )
                    val persisted = place("2", "카페B")
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(apiResponse)
                    coEvery { placeRepository.findByKakaoPlaceId("1") } returns place("1", "카페A")
                    coEvery { placeRepository.findByKakaoPlaceId("2") } returns null
                    every {
                        placeRepository.saveAll(match<List<Place>> {
                            it.map(Place::kakaoPlaceId) == listOf("2")
                        })
                    } returns Flux.just(persisted)
                    every { placeEnrichmentService.enrichPlaceData(persisted) } returns enrichmentJob

                    val savedCount = placeService.searchAndSavePlacesByKeyword(keyword, page)

                    savedCount shouldBe 1
                    verify(exactly = 1) { placeRepository.saveAll(any<List<Place>>()) }
                    verify(exactly = 1) { placeEnrichmentService.enrichPlaceData(persisted) }
                }
            }

            When("한 Kakao 응답에 같은 장소 ID가 반복될 경우") {
                Then("ID를 먼저 중복 제거하여 한 번만 조회하고 저장한다") {
                    val apiResponse = kakaoResponse(
                        kakaoPlace("same-id", "중복 카페"),
                        kakaoPlace("same-id", "중복 카페")
                    )
                    val savedPlaces = slot<List<Place>>()
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(apiResponse)
                    coEvery { placeRepository.findByKakaoPlaceId("same-id") } returns null
                    every { placeRepository.saveAll(capture(savedPlaces)) } answers {
                        Flux.fromIterable(savedPlaces.captured)
                    }
                    every { placeEnrichmentService.enrichPlaceData(any()) } returns enrichmentJob

                    placeService.searchAndSavePlacesByKeyword(keyword, page) shouldBe 1

                    savedPlaces.captured.map(Place::kakaoPlaceId) shouldBe listOf("same-id")
                    coVerify(exactly = 1) { placeRepository.findByKakaoPlaceId("same-id") }
                    verify(exactly = 1) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("검색된 장소가 모두 DB에 존재할 경우") {
                Then("저장과 Gemini 보강을 호출하지 않는다") {
                    val apiResponse = kakaoResponse(
                        kakaoPlace("1", "카페A"),
                        kakaoPlace("2", "카페B")
                    )
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(apiResponse)
                    coEvery { placeRepository.findByKakaoPlaceId(any()) } answers {
                        place(firstArg(), "기존 카페")
                    }

                    placeService.searchAndSavePlacesByKeyword(keyword, page) shouldBe 0

                    verify(exactly = 0) { placeRepository.saveAll(any<List<Place>>()) }
                    verify(exactly = 0) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("카테고리를 지원하지 않거나 주소에 구가 없는 경우") {
                Then("해당 장소를 저장하거나 보강하지 않는다") {
                    val apiResponse = kakaoResponse(
                        kakaoPlace("unsupported", "편의점", "편의점", "서울 마포구"),
                        kakaoPlace("no-district", "주소 없는 카페", "음식점 > 카페", "서울특별시")
                    )
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(apiResponse)
                    coEvery { placeRepository.findByKakaoPlaceId(any()) } returns null

                    placeService.searchAndSavePlacesByKeyword(keyword, page) shouldBe 0

                    verify(exactly = 0) { placeRepository.saveAll(any<List<Place>>()) }
                    verify(exactly = 0) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("유효 신규, DB 중복, 카테고리 무효, 주소 무효 장소가 섞여 있을 경우") {
                Then("유효한 신규 장소만 한 번 저장한다") {
                    val apiResponse = kakaoResponse(
                        kakaoPlace("new", "신규 카페"),
                        kakaoPlace("existing", "기존 카페"),
                        kakaoPlace("unsupported", "편의점", "편의점"),
                        kakaoPlace("no-district", "주소 없는 카페", address = "서울특별시")
                    )
                    val savedPlaces = slot<List<Place>>()
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(apiResponse)
                    coEvery { placeRepository.findByKakaoPlaceId("new") } returns null
                    coEvery { placeRepository.findByKakaoPlaceId("existing") } returns place("existing", "기존 카페")
                    coEvery { placeRepository.findByKakaoPlaceId("unsupported") } returns null
                    coEvery { placeRepository.findByKakaoPlaceId("no-district") } returns null
                    every { placeRepository.saveAll(capture(savedPlaces)) } answers {
                        Flux.fromIterable(savedPlaces.captured)
                    }
                    every { placeEnrichmentService.enrichPlaceData(any()) } returns enrichmentJob

                    placeService.searchAndSavePlacesByKeyword(keyword, page) shouldBe 1

                    savedPlaces.captured.map(Place::kakaoPlaceId) shouldBe listOf("new")
                    verify(exactly = 1) { placeRepository.saveAll(any<List<Place>>()) }
                    verify(exactly = 1) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("saveAll이 저장 요청 중 일부만 반환할 경우") {
                Then("실제 반환된 장소만 보강하고 실제 저장 수를 반환한다") {
                    val apiResponse = kakaoResponse(
                        kakaoPlace("1", "카페A"),
                        kakaoPlace("2", "카페B")
                    )
                    val persisted = place("2", "카페B")
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns Mono.just(apiResponse)
                    coEvery { placeRepository.findByKakaoPlaceId(any()) } returns null
                    every { placeRepository.saveAll(any<List<Place>>()) } returns Flux.just(persisted)
                    every { placeEnrichmentService.enrichPlaceData(persisted) } returns enrichmentJob

                    placeService.searchAndSavePlacesByKeyword(keyword, page) shouldBe 1

                    verify(exactly = 1) { placeEnrichmentService.enrichPlaceData(persisted) }
                    verify(exactly = 1) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("카테고리의 AND와 OR 조건 경계를 처리할 경우") {
                Then("현재 조건 우선순위에 맞는 카테고리만 저장한다") {
                    val categoryCases = listOf(
                        kakaoPlace("brunch", "브런치", "브런치"),
                        kakaoPlace("dessert-only", "디저트", "디저트"),
                        kakaoPlace("dessert-cafe", "디저트 카페", "디저트 카페"),
                        kakaoPlace("japanese-only", "일식", "일식"),
                        kakaoPlace("japanese-restaurant", "일식당", "일식 음식점"),
                        kakaoPlace("ramen", "라멘", "라멘"),
                        kakaoPlace("restaurant-only", "레스토랑", "레스토랑"),
                        kakaoPlace("course-restaurant", "코스", "레스토랑 코스"),
                        kakaoPlace("cooking-only", "요리", "요리"),
                        kakaoPlace("cooking-class", "요리 클래스", "요리 클래스"),
                        kakaoPlace("workshop", "공방", "공방"),
                        kakaoPlace("drawing-only", "그림", "그림"),
                        kakaoPlace("drawing-class", "그림 클래스", "그림 클래스"),
                        kakaoPlace("writing-only", "글쓰기", "글쓰기"),
                        kakaoPlace("writing-class", "글쓰기 클래스", "글쓰기 클래스")
                    )
                    val savedPlaces = slot<List<Place>>()
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns
                        Mono.just(KakaoApiResponse(categoryCases, Meta(true, categoryCases.size)))
                    coEvery { placeRepository.findByKakaoPlaceId(any()) } returns null
                    every { placeRepository.saveAll(capture(savedPlaces)) } answers {
                        Flux.fromIterable(savedPlaces.captured)
                    }
                    every { placeEnrichmentService.enrichPlaceData(any()) } returns enrichmentJob

                    placeService.searchAndSavePlacesByKeyword(keyword, page) shouldBe 9

                    savedPlaces.captured.associate { it.kakaoPlaceId to it.subCategory } shouldBe mapOf(
                        "brunch" to SubCategory.DESSERT_CAFE,
                        "dessert-cafe" to SubCategory.DESSERT_CAFE,
                        "japanese-restaurant" to SubCategory.SOLO_RESTAURANT,
                        "ramen" to SubCategory.SOLO_RESTAURANT,
                        "course-restaurant" to SubCategory.FINE_DINING,
                        "cooking-class" to SubCategory.WORKSHOP,
                        "workshop" to SubCategory.WORKSHOP,
                        "drawing-class" to SubCategory.ONE_DAY_CLASS,
                        "writing-class" to SubCategory.ONE_DAY_CLASS
                    )
                }
            }

            When("Kakao 조회가 실패할 경우") {
                Then("Mongo 조회, 저장, Gemini 보강을 시작하지 않는다") {
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns
                        Mono.error(IllegalStateException("Kakao 조회 실패"))

                    val exception = shouldThrow<IllegalStateException> {
                        placeService.searchAndSavePlacesByKeyword(keyword, page)
                    }

                    exception.message shouldBe "Kakao 조회 실패"
                    coVerify(exactly = 0) { placeRepository.findByKakaoPlaceId(any()) }
                    verify(exactly = 0) { placeRepository.saveAll(any<List<Place>>()) }
                    verify(exactly = 0) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }

            When("Mongo 일괄 저장이 실패할 경우") {
                Then("오류를 전파하고 Gemini 보강을 시작하지 않는다") {
                    every { kakaoApiService.searchPlacesByKeyword(keyword, page) } returns
                        Mono.just(kakaoResponse(kakaoPlace("1", "카페A")))
                    coEvery { placeRepository.findByKakaoPlaceId("1") } returns null
                    every { placeRepository.saveAll(any<List<Place>>()) } returns
                        Flux.error(IllegalStateException("Mongo 저장 실패"))

                    val exception = shouldThrow<IllegalStateException> {
                        placeService.searchAndSavePlacesByKeyword(keyword, page)
                    }

                    exception.message shouldBe "Mongo 저장 실패"
                    verify(exactly = 0) { placeEnrichmentService.enrichPlaceData(any()) }
                }
            }
        }

        Given("레벨별 장소 추천(getRecommendedPlacesByLevel) 시") {
            val level = Level.ONE

            When("해당 레벨의 장소가 존재하면") {
                Then("DB에서 조회된 장소 목록이 DTO로 변환되어 반환된다") {
                    val places = listOf(
                        place(
                            kakaoPlaceId = "1",
                            name = "추천카페A",
                            displayTitle = "AI 제목1",
                            displayTags = listOf("#태그1", null, "#태그2"),
                            urls = listOf("url1", "url2")
                        ),
                        place(
                            kakaoPlaceId = "2",
                            name = "추천카페B",
                            area = "서대문구",
                            displayTitle = "AI 제목2",
                            displayTags = listOf("#태그3"),
                            urls = listOf("url3")
                        )
                    )
                    coEvery { placeRepository.findRandom10ByLevel(level) } returns places

                    val result = placeService.getRecommendedPlacesByLevel(level)

                    result.size shouldBe 2
                    result[0].placeName shouldBe "추천카페A"
                    result[0].displayTitle shouldBe "AI 제목1"
                    result[0].imageUrl shouldBe "url1"
                    result[0].displayTags shouldBe listOf("#태그1", "#태그2")
                    result[1].area shouldBe "서대문구"
                    result[1].displayTags shouldBe listOf("#태그3")
                }
            }

            When("URL 목록이 비었거나 첫 URL이 빈 문자열일 경우") {
                Then("빈 목록은 null, 빈 문자열은 그대로 반환한다") {
                    coEvery { placeRepository.findRandom10ByLevel(level) } returns listOf(
                        place("empty-list", "URL 없음", urls = emptyList()),
                        place("blank-url", "빈 URL", urls = listOf("", "fallback"))
                    )

                    val result = placeService.getRecommendedPlacesByLevel(level)

                    result[0].imageUrl shouldBe null
                    result[1].imageUrl shouldBe ""
                }
            }

            When("해당 레벨의 장소가 없을 경우") {
                Then("빈 추천 목록을 반환한다") {
                    coEvery { placeRepository.findRandom10ByLevel(level) } returns emptyList()

                    placeService.getRecommendedPlacesByLevel(level) shouldBe emptyList()
                }
            }
        }
    }

    private fun kakaoResponse(vararg places: KakaoPlace): KakaoApiResponse {
        return KakaoApiResponse(places.toList(), Meta(true, places.size))
    }

    private fun kakaoPlace(
        id: String,
        name: String,
        category: String = "음식점 > 카페",
        address: String = "서울 마포구",
        url: String = "http://localhost/places/$id"
    ): KakaoPlace {
        return KakaoPlace(
            id = id,
            placeName = name,
            categoryName = category,
            addressName = address,
            roadAddressName = address,
            placeUrl = url
        )
    }

    private fun place(
        kakaoPlaceId: String,
        name: String,
        area: String = "마포구",
        displayTitle: String = name,
        displayTags: List<String?> = emptyList(),
        urls: List<String> = listOf("http://localhost/places/$kakaoPlaceId")
    ): Place {
        return Place(
            id = "id-$kakaoPlaceId",
            kakaoPlaceId = kakaoPlaceId,
            placeName = name,
            area = area,
            address = "서울 $area",
            description = name,
            mainCategory = MainCategory.RELAX_LEISURE,
            subCategory = SubCategory.CAFE,
            level = Level.ONE,
            kakaoCategoryName = "음식점 > 카페",
            displayTitle = displayTitle,
            displayTags = displayTags,
            urls = urls
        )
    }
}
