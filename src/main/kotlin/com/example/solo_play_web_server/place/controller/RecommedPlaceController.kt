package com.example.solo_play_web_server.place.controller

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.dtos.RecommendPlaceResponseDto
import com.example.solo_play_web_server.place.service.RecommendPlaceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/place/recommend")
class RecommedPlaceController(
    private val recommendPlaceService: RecommendPlaceService
) {
    @GetMapping
    suspend fun getRecommendedPlaces(
        @RequestParam level: Level
    ): Flux<RecommendPlaceResponseDto> {
        return recommendPlaceService.getPlaceByLevel(level)
    }
}