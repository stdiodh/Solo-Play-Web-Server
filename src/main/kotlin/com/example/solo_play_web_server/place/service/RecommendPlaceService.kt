package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.dtos.RecommendPlaceResponseDto
import com.example.solo_play_web_server.place.repository.PlaceRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class RecommendPlaceService (
    private val placeRepository: PlaceRepository
){
    suspend fun getPlaceByLevel(level: Level): Flux<RecommendPlaceResponseDto> {
        return placeRepository.findByLevel(level)
            .take(10)
            .map { place ->
                RecommendPlaceResponseDto(
                    placeId = place.id ?: "",
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