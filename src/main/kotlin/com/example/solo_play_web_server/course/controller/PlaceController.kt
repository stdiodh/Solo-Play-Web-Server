package com.example.solo_play_web_server.course.controller

import PlaceRequestDTO
import com.example.solo_play_web_server.common.dtos.BaseResponse
import com.example.solo_play_web_server.course.entity.Place
import com.example.solo_play_web_server.course.enums.Region
import com.example.solo_play_web_server.course.service.PlaceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Tag(name = "Place API", description = "장소 관리 API")
@RestController
@RequestMapping("/api/place")
class PlaceController(
    private val placeService: PlaceService
) {
    @Operation(summary = "장소 생성", description = "특정 지역에 장소를 생성합니다.")
    @PostMapping
    private suspend fun createPlace(@Valid @RequestBody placeRequestDto: PlaceRequestDTO): Mono<ResponseEntity<BaseResponse<Place>>> {
        return placeService.createPlace(placeRequestDto)
            .map { place ->
                ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse(data = place))
            }
    }


    @Operation(summary = "모든 장소 조회", description = "모든 장소를 조회합니다.")
    @GetMapping
    private suspend fun getAllPlaces(): Flux<ResponseEntity<BaseResponse<Place>>> {
        return placeService.getAllPlaces()
            .map { place ->
                ResponseEntity.status(HttpStatus.OK).body(BaseResponse(data = place))
            }
    }

    @Operation(summary = "지역별 장소 조회", description = "특정 지역의 장소들을 조회합니다.")
    @GetMapping("/{region}")
    private suspend fun getPlaceByRegion(
        @Parameter(description = "지역 정보") @PathVariable region: Region
    ): Mono<ResponseEntity<BaseResponse<List<Place>>>> {
        return placeService.getPlacesByRegion(region)
            .collectList()
            .map { places ->
                ResponseEntity.status(HttpStatus.OK).body(BaseResponse(data = places))
            }
    }

    @Operation(summary = "장소 이름으로 검색", description = "장소의 이름을 통해 장소를 검색합니다.")
    @GetMapping("/search")
    private suspend fun getPlaceByName(
        @RequestParam name: String
    ): Mono<ResponseEntity<BaseResponse<List<Place>>>> {
        return placeService.getPlacesByName(name)
            .collectList()
            .map { places ->
                ResponseEntity.status(HttpStatus.OK).body(BaseResponse(data = places))
            }
    }

    @Operation(summary = "장소 삭제", description = "장소 ID를 통해 장소를 삭제합니다.")
    @DeleteMapping("/{id}")
    private suspend fun deletePlace(
        @Parameter(description = "장소 ID") @PathVariable id: String
    ): Mono<ResponseEntity<BaseResponse<String>>> {
        return placeService.deletePlace(id)
            .map { ResponseEntity.status(HttpStatus.OK).body(BaseResponse(data = "장소 삭제 완료")) }
    }
}
