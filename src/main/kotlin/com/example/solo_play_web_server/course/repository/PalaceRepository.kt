package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Palace
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PalaceRepository : CoroutineCrudRepository<Palace, Long?>{
    @Query("SELECT * FROM palace")
    suspend fun findAllPalaces() : List<Palace>

    @Query("SELECT * FROM palace WHERE id = :id")
    suspend fun findById(id: Long) : Palace?

    @Query("DELETE FROM palace WHERE id = :id")
    suspend fun deleteById(id : Long)
}