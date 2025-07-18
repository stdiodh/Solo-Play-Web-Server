package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.dtos.PlaceRequestDTO
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

    suspend fun generateRandomPlaces(level: Level): List<Place> {
        val randomPlaces = (1..10).map {
            Place(
                name = "레벨 $level 테스트 장소 $it",
                description = "레벨 $level 에 자동 생성 설명입니다.",
                region = Area.entries.random(),
                level = level,
                tags = listOf("예시 태그1", "예시 태그2", "예시 태그3"),
                urls = listOf("https://example.com/image$it.jpg")
            )
        }
        return placeRepository.saveAll(randomPlaces).collectList().awaitSingle()
    }
}