package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.common.exception.CommonNotFoundException
import com.example.solo_play_web_server.place.dtos.PlaceRequestDTO
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.PlaceCategory
import com.example.solo_play_web_server.place.enums.Region
import com.example.solo_play_web_server.place.repository.PlaceRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class PlaceServiceTest : StringSpec() {
    @MockK
    lateinit var placeRepository: PlaceRepository
    lateinit var placeService: PlaceService

    override suspend fun beforeTest(testCase: TestCase) {
        MockKAnnotations.init(this)
        placeService = PlaceService(placeRepository)
    }

    init{
        val examplePlace = PlaceRequestDTO (
            _name = "카페",
            _region = Region.GANGNAM,
            _description = "카페 설명",
            _placeCategory = PlaceCategory.CAFE,
            _urls = listOf("url")
        )

        "getAllPlaces() 메소드를 실행하면 장소를 모두 가져온다" {
            val place1 = examplePlace.toEntity()
            val place2 = examplePlace.copy(_name = "카페2").toEntity()

            coEvery { placeService.getAllPlaces() } returns Flux.just(place1, place2)

            val resultFlux = placeService.getAllPlaces()

            StepVerifier.create(resultFlux)
                .expectNext(place1)
                .expectNext(place2)
                .verifyComplete()
        }

        "createPlaces() 메소드로 장소를 생성할 수 있다" {
            val expectedPlace = examplePlace.toEntity()

            coEvery { placeRepository.save(any()) } returns Mono.just(expectedPlace)

            val result = placeService.createPlace(examplePlace)

            StepVerifier.create(result)
                .expectNextMatches { it.name == "카페" && it.region == Region.GANGNAM }
                .verifyComplete()

            coVerify(exactly = 1) { placeRepository.save(any()) }
        }


        "updatePlace 는 기존 장소 정보를 갱신한다." {
            val id = "1"
            val old = Place(id, "Old", Region.MAPO, "old", 0, PlaceCategory.CAFE, emptyList())
            val newDto = PlaceRequestDTO(
                _name = "New",
                _region = Region.GANGNAM,
                _description = "new",
                _placeCategory = PlaceCategory.CAFE,
                _urls = listOf("url"))

            val updated = old.copy(
                name = newDto.name,
                region = newDto.region,
                description = newDto.description,
                urls = newDto.urls
            )

            coEvery { placeRepository.findById(id) } returns Mono.just(old)
            coEvery { placeRepository.save(updated) } returns Mono.just(updated)

            val result = placeService.updatePlace(id, newDto)

            StepVerifier.create(result)
                .expectNextMatches { it.name == "New" && it.region == Region.GANGNAM }
                .verifyComplete()

            coVerify(exactly = 1) { placeRepository.save(any()) }
        }


        "deletePlace 는 기존 장소 정보를 삭제한다."{
            val id = "1"

            coEvery { placeRepository.deleteById(id) } returns Mono.empty()

            val result = placeService.deletePlace(id)

            StepVerifier.create(result)
                .verifyComplete()

            coVerify { placeRepository.deleteById(id) }
        }

        "getRegionsByZone 메서드는 해당 지역에 해당하는 구의 리스트로 가져온다" {
            val result = placeService.getRegionsByZone(Region.Zone.GANGNAM)
            result shouldContainExactly listOf("강남구", "서초구")
        }


        "getPlacesByRegion은 해당 지역의 장소들을 반환한다" {
            val region = "강남구"
            val data = listOf(
                Place("1", "A", Region.GANGNAM, "desc", 1, PlaceCategory.CAFE, listOf())
            )

            coEvery { placeRepository.findByRegion(region) } returns Flux.fromIterable(data)

            val result = placeService.getPlacesByRegion(region)

            StepVerifier.create(result.collectList())
                .expectNext(data)
                .verifyComplete()
        }

        "getPlacesByName은 해당 이름을 포함하는 장소들을 반환한다" {
            val name = "카페"
            val data = listOf(
                Place("1", "좋은카페", Region.MAPO, "desc", 1, PlaceCategory.CAFE, listOf())
            )

            coEvery { placeRepository.findByNameContaining(name) } returns Flux.fromIterable(data)

            val result = placeService.getPlacesByName(name)

            StepVerifier.create(result.collectList())
                .expectNext(data)
                .verifyComplete()
        }

        "incrementSaved 함수를 사용하면 saved 가 1 증가한다." {
            val placeId = "1"
            val originalPlace = Place (
                id = placeId,
                name = "Place 1",
                region = Region.GANGNAM,
                description = "desc",
                saved = 10,
                placeCategory = PlaceCategory.CAFE,
                urls = emptyList()
            )
            val updatePlace = originalPlace.copy(saved = originalPlace.saved + 1)

            coEvery { placeRepository.findById(placeId) } returns Mono.just(originalPlace)
            coEvery { placeRepository.save(updatePlace) } returns Mono.just(updatePlace)

            val result = placeService.incrementSaved(placeId)

            StepVerifier.create(result)
                .expectNextMatches { it.saved == 11 }
                .verifyComplete()

            coVerify(exactly = 1) { placeRepository.findById(placeId) }
            coVerify(exactly = 1) { placeRepository.save(updatePlace) }
        }


        "incrementSaved 함수에서 게시물을 찾지 못하면 CommonNotFoundException 을 던진다." {
            val placeId = "not_exist"

            coEvery { placeRepository.findById(placeId) } returns Mono.empty()

            val result = placeService.incrementSaved(placeId)

            StepVerifier.create(result)
                .expectError(CommonNotFoundException::class.java)
                .verify()

            coVerify(exactly = 1) { placeRepository.findById(placeId) }
        }

        "getTop10PlacesBySaved() 메서드는 saved 수 상위 10개의 장소를 가져온다." {
            val places = (1 .. 20).map {
                Place(
                    id = it.toString(),
                    name = "Place $it",
                    region = Region.MAPO,
                    description = "설명 $it",
                    saved = it * 10,
                    placeCategory = PlaceCategory.CAFE,
                    urls = emptyList()
                )
            }.shuffled()

            coEvery { placeRepository.findAll() } returns Flux.fromIterable(places)

            val expectedTop10 = places
                .sortedByDescending { it.saved }
                .take(10)

            val result = placeService.getTop10PlacesBySaved()

            StepVerifier.create(result)
                .expectNextSequence(expectedTop10)
                .verifyComplete()

            coVerify(exactly = 1) { placeRepository.findAll() }
        }


        "getTop6PlaceByCategory() 메서드는 카테고리 별로 saved 수에 따른 상위 6개의 장소를 가져온다." {
            val category = PlaceCategory.CAFE
            val places = (1..6).map {
                Place(
                    id = it.toString(),
                    name = "CAFE $it",
                    region = Region.GANGNAM,
                    description = "설명 $it",
                    saved = 100 - it,
                    placeCategory = PlaceCategory.CAFE,
                    urls = emptyList()
                )
            }
            coEvery { placeRepository.findTop6ByPlaceCategoryOrderBySavedDesc(category) } returns Flux.fromIterable(places)

            val result = placeService.getTop6PlacesByCategory(category)

            StepVerifier.create(result)
                .expectNextSequence(places)
                .verifyComplete()

            coVerify(exactly = 1) { placeRepository.findTop6ByPlaceCategoryOrderBySavedDesc(category) }
        }
    }


}