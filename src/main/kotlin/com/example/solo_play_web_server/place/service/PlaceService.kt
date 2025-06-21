package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.common.exception.CommonNotFoundException
import com.example.solo_play_web_server.place.dtos.PlaceRequestDTO
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.PlaceCategory
import com.example.solo_play_web_server.place.enums.Region
import com.example.solo_play_web_server.place.repository.PlaceRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Service
class PlaceService(
    private val placeRepository: PlaceRepository,
) {
    suspend fun createPlace(requestDto: PlaceRequestDTO): Mono<Place> {
        val place = requestDto.toEntity()
        return placeRepository.save(place)
    }

    suspend fun getAllPlaces(): Flux<Place> {
        return placeRepository.findAll()
    }

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

    suspend fun deletePlace(id: String): Mono<Void> {
        return placeRepository.deleteById(id)
    }

    fun getRegionsByZone(zone: Region.Zone): List<String> {
        return Region.entries
            .filter { it.zone == zone }
            .map { it.districtName }
    }

    suspend fun getPlacesByRegion(regionName: String): Flux<Place> {
        return placeRepository.findByRegion(regionName)
    }

    suspend fun getPlacesByName(name: String): Flux<Place> {
        return placeRepository.findByNameContaining(name)
    }

    suspend fun incrementLiked(id: String): Mono<Place> {
        return placeRepository.findById(id)
            .switchIfEmpty(Mono.error(CommonNotFoundException("장소 ID $id 를 찾지 못하였습니다!")))
            .flatMap { place ->
                val updatedPlace = place.copy(liked = place.liked + 1)
                placeRepository.save(updatedPlace)
            }
    }

    suspend fun getTop10PlacesByLiked(): Flux<Place> {
        return placeRepository.findAll()
            .sort { place1, place2 -> place2.liked.compareTo(place1.liked) }
            .take(10)
    }

    fun findTop6ByPlaceCategoryOrderBySavedDesc(@PathVariable category : PlaceCategory) : Flux<Place> {
        return placeRepository.findTop6ByPlaceCategoryOrderByLikedDesc(category)
    }
}
