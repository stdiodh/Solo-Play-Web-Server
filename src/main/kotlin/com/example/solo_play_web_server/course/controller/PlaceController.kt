package com.example.solo_play_web_server.course.controller

import com.example.solo_play_web_server.common.dtos.BaseResponse
import com.example.solo_play_web_server.course.dtos.PlaceRequestDto
import com.example.solo_play_web_server.course.entity.Place
import com.example.solo_play_web_server.course.enums.Region
import com.example.solo_play_web_server.course.service.PlaceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Place API", description = "장소 관리 API")
@RestController
@RequestMapping("api/place")
class PlaceController (
    private val placeService: PlaceService
) {
    /**
     * 장소를 생성하는 API
     * @return ResponseEntity<BaseResponse<Place>> - 생성한 장소에 대한 정보 반환
     */
    @Operation(summary = "장소 생성", description = "특정 지역에 장소를 생성합니다.")
    @PostMapping
    suspend fun createPlace(@RequestBody @Valid placeRequestDto: PlaceRequestDto) : ResponseEntity<BaseResponse<Place>>{
        val result = placeService.createPlace(placeRequestDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse(result))
    }

    /**
     * 특정 지역의 장소를 불러오는 API
     * @return ResponseEntity<BaseResponse<List<Place>>> - 특정 지역에 대한 장소에 대한 정보를 리스트로 반환
     */
    @Operation(summary = "특정 지역 장소 조회", description = "특정 지역의 장소를 조회합니다.")
    @GetMapping("/region/{region}")
    suspend fun getPlaceByRegion(@PathVariable region: Region) : ResponseEntity<BaseResponse<List<Place>>>{
        val result = placeService.getPlacesByRegion(region)
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse(result))
    }
}