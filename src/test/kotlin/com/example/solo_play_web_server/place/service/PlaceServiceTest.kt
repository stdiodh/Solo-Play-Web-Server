package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.common.exception.CommonNotFoundException
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.PlaceCategory
import com.example.solo_play_web_server.place.enums.Region
import com.example.solo_play_web_server.place.repository.PlaceRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
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


        "getTop6PlaceByCategory() 메서드를 실행하면 카테고리 별로 saved 수에 따른 상위 6개의 장소를 가져온다." {
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