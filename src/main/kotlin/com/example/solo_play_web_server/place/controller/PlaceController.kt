package com.example.solo_play_web_server.place.controller

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.dtos.PlaceRequestDTO
import com.example.solo_play_web_server.place.dtos.RecommendPlaceResponseDto
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.service.PlaceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Tag(name = "Place API", description = "장소 생성/삭제/조회 API")
@RestController
@RequestMapping("/api/place")
class PlaceCommandController(
    private val placeService: PlaceService
) {

    @Operation(summary = "모든 장소 조회", description = "데이터베이스에 저장된 모든 장소를 반환합니다.")
    @GetMapping
    suspend fun getAllPlace(): ResponseEntity<Flux<Place>> {
        val result = placeService.getAllPlace()
        return ResponseEntity.status(HttpStatus.OK).body(result)
    }

    @Operation(summary = "장소 생성", description = "장소 정보를 입력받아 새 장소를 하나 생성합니다.")
    @PostMapping
    suspend fun createPlace(
        @RequestBody dto: PlaceRequestDTO
    ): ResponseEntity<Mono<Place>> {
        val saved = placeService.createPlace(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }

    @Operation(summary = "장소 삭제", description = "장소 ID를 통해 해당 장소를 삭제합니다.")
    @DeleteMapping("/{id}")
    suspend fun deletePlace(
        @Parameter(description = "삭제할 장소의 ID") @PathVariable id: String
    ): ResponseEntity<String> {
        val deleted = placeService.deletePlace(id)
        return if (deleted) ResponseEntity.status(HttpStatus.OK).body("$id 장소 삭제완료")
        else ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @Operation(summary = "레벨 기반 장소 조회", description = "레벨(one, two, three)을 기준으로 무작위 장소 10개를 조회합니다.")
    @GetMapping("/recommend")
    suspend fun getRecommendedPlaces(
        @RequestParam level: Level
    ): Flux<RecommendPlaceResponseDto> {
        return placeService.getRandomPlacesByLevel(level)
    }
}
