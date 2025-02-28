package com.example.solo_play_web_server.course.service

import PlaceRequestDTO
import com.example.solo_play_web_server.course.entity.Place
import com.example.solo_play_web_server.course.enums.Region
import com.example.solo_play_web_server.course.repository.PlaceRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Service
class PlaceService(
    private val placeRepository: PlaceRepository,
) {
    /**
     * 장소를 생성하는 메소드
     */
    suspend fun createPlace(requestDto: PlaceRequestDTO): Mono<Place> {
        val place = requestDto.toEntity()
        return placeRepository.save(place)
    }

    /**
     * 전체 장소를 조회하는 메소드
     */
    suspend fun getAllPlaces(): Flux<Place> {
        return placeRepository.findAll()
    }

    /**
     * 특정 지역에 해당하는 장소를 조회하는 메소드
     */
    suspend fun getPlacesByRegion(region: Region): Flux<Place> {
        return placeRepository.findByRegion(region.name)
    }

    /**
     * 장소 이름을 통해 장소들을 검색하는 메소드
     */
    suspend fun getPlacesByName(name: String): Flux<Place> {
        return placeRepository.findByNameContaining(name)
    }

    /**
     * 장소 ID를 통해 삭제하는 메소드
     */
    suspend fun deletePlace(id: String): Mono<Void> {
        return placeRepository.deleteById(id)
    }
}
