package com.example.solo_play_web_server.place.controller

import PlaceRequestDTO
import com.example.solo_play_web_server.common.dtos.BaseResponse
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.Region
import com.example.solo_play_web_server.place.service.PlaceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Tag(name = "Place API", description = "장소 관리 API")
@RestController
@RequestMapping("/api/place")
class PlaceController(
    private val placeService: PlaceService
) {
    @Operation(summary = "장소 생성", description = "특정 지역에 장소를 생성합니다.")
    @PostMapping
    private suspend fun createPlace(@Valid @RequestBody placeRequestDto: PlaceRequestDTO): Mono<BaseResponse<Place>> {
        return placeService.createPlace(placeRequestDto)
            .map { place -> BaseResponse(data = place) }
    }

    @Operation(summary = "모든 장소 조회", description = "모든 장소를 조회합니다.")
    @GetMapping
    private suspend fun getAllPlaces(): Flux<BaseResponse<Place>> {
        return placeService.getAllPlaces()
            .map { place -> BaseResponse(data = place) }
    }

    @Operation(summary = "장소 수정", description = "장소 정보를 수정합니다.")
    @PutMapping("/{id}")
    private suspend fun updatePlace(
        @PathVariable id: String,
        @Valid @RequestBody placeRequestDto: PlaceRequestDTO
    ): Mono<BaseResponse<Place>> {
        return placeService.updatePlace(id, placeRequestDto)
            .map { place -> BaseResponse(data = place) }
    }

    @Operation(summary = "장소 삭제", description = "장소 ID를 통해 장소를 삭제합니다.")
    @DeleteMapping("/{id}")
    private suspend fun deletePlace(
        @Parameter(description = "장소 ID") @PathVariable id: String
    ): Mono<BaseResponse<String>> {
        return placeService.deletePlace(id)
            .map { BaseResponse(data = "장소 삭제 완료") }
    }


    @Operation(summary = "지역에 해당하는 구 목록 조회", description = "특정 지역(zone)에 해당하는 구들을 조회합니다.")
    @GetMapping("/zone/{zone}/regions")
    fun getRegionsByZone(
        @Parameter(description = "지역(zone) 정보") @PathVariable zone: Region.Zone
    ): Mono<BaseResponse<List<String>>> {
        val regions = placeService.getRegionsByZone(zone)
        return Mono.just(BaseResponse(data = regions))
    }

    @Operation(summary = "구에 해당하는 장소 조회", description = "특정 구(region)에 해당하는 장소들을 조회합니다.")
    @GetMapping("/region/{region}/places")
    suspend fun getPlacesByRegion(
        @Parameter(description = "구(region) 정보") @PathVariable region: String
    ): Mono<BaseResponse<List<Place>>> {
        val places = placeService.getPlacesByRegion(region)
            .collectList()
        return places.map { BaseResponse(data = it) }
    }

    @Operation(summary = "장소 이름으로 검색", description = "장소의 이름을 통해 장소를 검색합니다.")
    @GetMapping("/search")
    private suspend fun getPlaceByName(
        @RequestParam name: String
    ): Mono<BaseResponse<List<Place>>> {
        return placeService.getPlacesByName(name)
            .collectList()
            .map { places -> BaseResponse(data = places) }
    }

    @Operation(summary = "저장됨 수 증가", description = "특정 장소의 저장됨 수를 증가시킵니다.")
    @PostMapping("/{id}/saved")
    private suspend fun incrementSaved(
        @Parameter(description = "장소 ID") @PathVariable id: String
    ): Mono<BaseResponse<Place>> {
        return placeService.incrementSaved(id)
            .map { place -> BaseResponse(data = place) }
            .onErrorResume { e ->
                Mono.just(BaseResponse(status = "ERROR", resultMsg = e.message ?: "An error occurred"))
            }
    }

    @Operation(summary = "저장됨 순위 상위 10개 장소 조회", description = "저장됨 수 기준으로 상위 10개 장소를 조회합니다.")
    @GetMapping("/top10/saved")
    private suspend fun getTop10PlacesBySaved(): Flux<BaseResponse<Place>> {
        return placeService.getTop10PlacesBySaved()
            .map { place -> BaseResponse(data = place) }
    }
}
