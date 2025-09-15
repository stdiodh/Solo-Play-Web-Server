package com.example.solo_play_web_server.place.repository

import com.example.solo_play_web_server.place.entity.Place
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PlaceRepository : ReactiveMongoRepository<Place, String>{
    suspend fun findByKakaoPlaceId(kakaoPlaceId: String): Place?
}