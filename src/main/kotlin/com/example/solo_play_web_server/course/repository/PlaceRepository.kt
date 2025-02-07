package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Place
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PlaceRepository : CoroutineCrudRepository<Place, Long>{
    /**
     * 특정 지역에 해당하는 장소를 가져오기 위한 쿼리
     */
    @Query("SELECT * FROM place WHERE region = :region")
    suspend fun findByRegion(region: String): List<Place>
}