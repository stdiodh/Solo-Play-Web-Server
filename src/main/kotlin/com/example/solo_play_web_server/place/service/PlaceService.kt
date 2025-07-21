package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.dtos.PlaceRequestDTO
import com.example.solo_play_web_server.place.dtos.RecommendPlaceResponseDto
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.Area
import com.example.solo_play_web_server.place.repository.PlaceRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PlaceService (
    private val placeRepository: PlaceRepository
) {
    suspend fun getAllPlace() : Flux<Place> {
        return placeRepository.findAll()
    }

    suspend fun createPlace(dto: PlaceRequestDTO): Mono<Place> {
        return placeRepository.save(dto.toEntity())
    }

    suspend fun deletePlace(id: String): Boolean {
        return try {
            placeRepository.deleteById(id).awaitSingleOrNull()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getRandomPlacesByLevel(level: Level): Flux<RecommendPlaceResponseDto> {
        return placeRepository.findByLevel(level)
            .collectList() // 전체 리스트 수집
            .map { it.shuffled().take(10) }
            .flatMapMany { Flux.fromIterable(it) }
            .map { place ->
                RecommendPlaceResponseDto(
                    id = place.id ?: "",
                    name = place.name,
                    area = place.region,
                    description = place.description,
                    level = place.level,
                    tags = place.tags,
                    urls = place.urls
                )
            }
    }
}