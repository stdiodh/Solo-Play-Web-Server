package com.example.solo_play_web_server.place.repository

import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enum.Level
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PlaceRepository : ReactiveMongoRepository<Place, String>{
    suspend fun findByKakaoPlaceId(kakaoPlaceId: String): Place?
    suspend fun findRandom10ByLevel(level: Level): List<Place>
}