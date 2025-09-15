package com.example.solo_play_web_server.place.controller

import com.example.solo_play_web_server.common.dto.ApiResponse
import com.example.solo_play_web_server.common.dto.ResultStatus
import com.example.solo_play_web_server.place.service.PlaceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/places")
class PlaceController (
    private val placeService: PlaceService
){
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
}