package com.example.solo_play_web_server.place.repository

import com.example.solo_play_web_server.place.entity.Place
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface PlaceRepository : ReactiveMongoRepository<Place, String> {
    suspend fun findByRegion(region: String): Flux<Place>
    suspend fun findByNameContaining(name: String): Flux<Place>
}
