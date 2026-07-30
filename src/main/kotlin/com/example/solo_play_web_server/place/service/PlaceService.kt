package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.dto.RecommendPlaceByLevelResponse
import com.example.solo_play_web_server.place.entity.KakaoPlace
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.enums.SubCategory
import com.example.solo_play_web_server.place.repository.PlaceRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceService (
    private val kakaoApiService: KakaoApiService,
    private val placeRepository: PlaceRepository,
    private val placeEnrichmentService: PlaceEnrichmentService
){
    @Transactional
    suspend fun searchAndSavePlacesByKeyword(keyword: String, page: Int): Int {
        val apiResponse = kakaoApiService.searchPlacesByKeyword(keyword, page).awaitSingle()

        val placesToSave = mutableListOf<Place>()
        for (kakaoPlace in apiResponse.documents.distinctBy { it.id }) {
            if (placeRepository.findByKakaoPlaceId(kakaoPlace.id) == null) {
                val place = toPlaceDocument(kakaoPlace)
                if (place != null) {
                    placesToSave.add(place)
                }
            }
        }

        if (placesToSave.isEmpty()) {
            return 0
        }

        val savedPlaces = placeRepository.saveAll(placesToSave).asFlow().toList()
        savedPlaces.forEach { place ->
            placeEnrichmentService.enrichPlaceData(place)
        }
        return savedPlaces.size
    }

    private fun toPlaceDocument(kakaoPlace: KakaoPlace): Place? {
        val subCategory = mapToSubCategory(kakaoPlace.categoryName) ?: return null
        val area = extractDistrictFromAddress(kakaoPlace.addressName) ?: return null

        return Place(
            kakaoPlaceId = kakaoPlace.id,
            placeName = kakaoPlace.placeName,
            area = area,
            address = kakaoPlace.addressName,
            description = kakaoPlace.placeName,
            mainCategory = subCategory.mainCategory,
            subCategory = subCategory,
            level = Level.ONE,
            kakaoCategoryName = kakaoPlace.categoryName,
            displayTitle = kakaoPlace.placeName,
            displayTags = listOf(),
            urls = listOf(kakaoPlace.placeUrl),
        )
    }

    private fun extractDistrictFromAddress(address: String): String? {
        return address.split(" ").find { it.endsWith("구") }
    }

    private fun mapToSubCategory(kakaoCategory: String): SubCategory? {
        return when {
            // Relax / Leisure
            "브런치" in kakaoCategory || "디저트" in kakaoCategory && "카페" in kakaoCategory -> SubCategory.DESSERT_CAFE
            "북카페" in kakaoCategory -> SubCategory.CAFE
            "카페" in kakaoCategory -> SubCategory.CAFE
            "공원" in kakaoCategory -> SubCategory.PARK
            "스파" in kakaoCategory || "찜질방" in kakaoCategory || "온천" in kakaoCategory -> SubCategory.SPA
            "도서관" in kakaoCategory -> SubCategory.LIBRARY

            // Culture / Art
            "영화관" in kakaoCategory -> SubCategory.CINEMA
            "미술관" in kakaoCategory || "박물관" in kakaoCategory || "전시" in kakaoCategory -> SubCategory.MUSEUM
            "연극" in kakaoCategory || "뮤지컬" in kakaoCategory || "콘서트" in kakaoCategory || "공연장" in kakaoCategory -> SubCategory.THEATER
            "서점" in kakaoCategory -> SubCategory.BOOKSTORE

            // Activity / Experience
            "헬스" in kakaoCategory || "클라이밍" in kakaoCategory || "볼링" in kakaoCategory || "탁구" in kakaoCategory || "골프" in kakaoCategory -> SubCategory.SPORTS
            "VR" in kakaoCategory || "AR" in kakaoCategory -> SubCategory.VR_AR
            "방탈출" in kakaoCategory || "보드게임" in kakaoCategory -> SubCategory.ESCAPE_ROOM
            "요리" in kakaoCategory && "클래스" in kakaoCategory || "공방" in kakaoCategory || "도예" in kakaoCategory || "베이킹" in kakaoCategory -> SubCategory.WORKSHOP

            // Travel / Exploration
            "산" in kakaoCategory || "바다" in kakaoCategory || "캠핑" in kakaoCategory -> SubCategory.NEAR_TRIP
            "시장" in kakaoCategory || "랜드마크" in kakaoCategory || "테마거리" in kakaoCategory -> SubCategory.CITY_TOUR
            "게스트하우스" in kakaoCategory || "호텔" in kakaoCategory -> SubCategory.SOLO_STAY

            // Gourmet / Food
            "라멘" in kakaoCategory || "일식" in kakaoCategory && "음식점" in kakaoCategory -> SubCategory.SOLO_RESTAURANT
            "길거리음식" in kakaoCategory || "야시장" in kakaoCategory -> SubCategory.STREET_FOOD
            "파인다이닝" in kakaoCategory || "레스토랑" in kakaoCategory && "코스" in kakaoCategory -> SubCategory.FINE_DINING

            // Healing / Self Improvement
            "요가" in kakaoCategory || "명상" in kakaoCategory -> SubCategory.YOGA_MEDITATION
            "원데이클래스" in kakaoCategory || "그림" in kakaoCategory && "클래스" in kakaoCategory || "글쓰기" in kakaoCategory && "클래스" in kakaoCategory -> SubCategory.ONE_DAY_CLASS
            "스터디카페" in kakaoCategory -> SubCategory.STUDY_CAFE
            "상담" in kakaoCategory || "코칭" in kakaoCategory -> SubCategory.COUNSELING

            // Special Experience
            "놀이공원" in kakaoCategory || "테마파크" in kakaoCategory -> SubCategory.THEME_PARK
            "노래방" in kakaoCategory -> SubCategory.KARAOKE
            "해외여행" in kakaoCategory -> SubCategory.OVERSEAS_TRAVEL
            "풀코스" in kakaoCategory -> SubCategory.FULL_COURSE_MEAL

            else -> null
        }
    }

    suspend fun getRecommendedPlacesByLevel(level: Level): List<RecommendPlaceByLevelResponse> {
        val places = placeRepository.findRandom10ByLevel(level)
        return places.map { place ->
            RecommendPlaceByLevelResponse(
                level = place.level,
                imageUrl = place.urls.firstOrNull(),
                placeName = place.placeName,
                displayTitle = place.displayTitle,
                area = place.area,
                displayTags = place.displayTags.filterNotNull()
            )
        }
    }
}
