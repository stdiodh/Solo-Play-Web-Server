package com.example.solo_play_web_server.place.repository

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.entity.Place
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface PlaceRepository : ReactiveMongoRepository<Place, String>{
    fun findByLevel(level: Level): Flux<Place>
}
