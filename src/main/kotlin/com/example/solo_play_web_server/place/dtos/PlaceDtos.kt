package com.example.solo_play_web_server.place.dtos

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.enums.Area

data class PlaceRequestDTO(
    private val name: String,
    private val description: String,
    private val region: Area,
    private val level : Level,
    private val tags : List<String>,
    private val urls: List<String>,
){
    fun toEntity(): Place {
        return Place(
            id = null,
            name = name,
            description = description,
            region = region,
            level = level,
            tags = tags,
            urls = urls
        )
    }
}
