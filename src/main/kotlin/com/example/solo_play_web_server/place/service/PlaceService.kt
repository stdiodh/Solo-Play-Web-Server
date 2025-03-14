package com.example.solo_play_web_server.place.service

import PlaceRequestDTO
import com.example.solo_play_web_server.common.exception.CommonNotFoundException
import com.example.solo_play_web_server.course.entity.Course
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.Region
import com.example.solo_play_web_server.place.repository.PlaceRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Service
class PlaceService(
    private val placeRepository: PlaceRepository,
) {
    // 장소 생성
    suspend fun createPlace(requestDto: PlaceRequestDTO): Mono<Place> {
        val place = requestDto.toEntity()
        return placeRepository.save(place)
    }

    // 전체 장소 조회
    suspend fun getAllPlaces(): Flux<Place> {
        return placeRepository.findAll()
    }

    // 장소 수정
    suspend fun updatePlace(id: String, requestDto: PlaceRequestDTO): Mono<Place> {
        return placeRepository.findById(id)
            .flatMap { existingPlace ->
                val updatedPlace = existingPlace.copy(
                    name = requestDto.name,
                    region = requestDto.region,
                    description = requestDto.description,
                    urls = requestDto.urls
                )
                placeRepository.save(updatedPlace)
            }
    }

    // 장소 삭제
    suspend fun deletePlace(id: String): Mono<Void> {
        return placeRepository.deleteById(id)
    }

    // 지역별 장소 조회
    suspend fun getPlacesByRegion(region: Region): Flux<Place> {
        return placeRepository.findByRegion(region.name)
    }

    // 이름으로 장소 조회
    suspend fun getPlacesByName(name: String): Flux<Place> {
        return placeRepository.findByNameContaining(name)
    }

    // 저장된 수 증가
    suspend fun incrementSaved(id: String): Mono<Place> {
        return placeRepository.findById(id)
            .switchIfEmpty(Mono.error(CommonNotFoundException("장소 ID $id 를 찾지 못하였습니다!")))
            .flatMap { place ->
                val updatedPlace = place.copy(saved = place.saved + 1)
                placeRepository.save(updatedPlace)
            }
    }
}
