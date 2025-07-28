package com.example.solo_play_web_server.place.dtos

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.enums.Area

data class RecommendPlaceResponseDto(
    val id : String,
    val name : String,
    val description : String,
    val area : Area,
    val level : Level,
    val tags : List<String>,
    val url : String
)