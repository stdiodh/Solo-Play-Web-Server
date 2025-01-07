package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Palace
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PalaceRepository : CoroutineCrudRepository<Palace, Long?>{
    @Query("SELECT * FROM palace")
    suspend fun findAllPalaces() : List<Palace>
}