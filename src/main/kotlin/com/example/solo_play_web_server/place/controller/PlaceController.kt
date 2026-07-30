package com.example.solo_play_web_server.place.controller

import com.example.solo_play_web_server.common.dto.ApiResponse
import com.example.solo_play_web_server.common.dto.ResultStatus
import com.example.solo_play_web_server.place.dto.RecommendPlaceByLevelResponse
import com.example.solo_play_web_server.place.enum.Level
import com.example.solo_play_web_server.place.service.PlaceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "장소 API 컨트롤러", description = "장소 검색 및 추천 API 명세서입니다.")
@RestController
@RequestMapping("/api/places")
class PlaceController (
    private val placeService: PlaceService
){
    @Operation(
        summary = "[Admin] 카카오 API를 이용한 장소 검색 및 저장",
        description = "키워드를 통해 카카오 지도의 장소 정보를 검색하고, 중복되지 않은 새로운 장소를 DB에 저장합니다. 관리자 권한이 필요합니다."
    )
    @GetMapping
    suspend fun searchAndSavePlaces(@RequestParam keyword: String, @RequestParam(defaultValue = "1") page: Int)
            : ResponseEntity<ApiResponse<Int>>{
        return try {
            val count = placeService.searchAndSavePlacesByKeyword(keyword, page)

            val response = ApiResponse(
                status = ResultStatus.SUCCESS,
                message = "'$keyword' 검색 결과, $count 개의 새로운 장소를 저장했습니다.",
                data = count
            )
            ResponseEntity.ok(response)

        } catch (e: Exception) {
            val response = ApiResponse<Int>(
                status = ResultStatus.ERROR,
                message = e.message ?: "장소 검색 중 알 수 없는 에러가 발생했습니다."
            )
            ResponseEntity.internalServerError().body(response)
        }
    }

    @Operation(
        summary = "[User] 레벨별 추천 장소 랜덤 조회",
        description = "사용자의 레벨에 맞는 장소 10개를 무작위로 추천하여 반환합니다."
    )
    @GetMapping("/recommendations")
    suspend fun getRecommandedPlaces(@RequestParam level: Level): ResponseEntity<ApiResponse<List<RecommendPlaceByLevelResponse>>>{
        return try {
            val recommendations = placeService.getRecommendedPlacesByLevel(level)

            val response = ApiResponse(
                status = ResultStatus.SUCCESS,
                message = "레벨 ${level.label} 추천 장소 목록입니다.",
                data = recommendations
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            val response = ApiResponse<List<RecommendPlaceByLevelResponse>>(
                status = ResultStatus.ERROR,
                message = e.message ?: "추천 장소 조회 중 알 수 없는 에러가 발생했습니다."
            )
            ResponseEntity.internalServerError().body(response)
        }
    }
}