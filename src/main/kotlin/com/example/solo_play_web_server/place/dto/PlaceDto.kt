package com.example.solo_play_web_server.place.dto

import com.example.solo_play_web_server.place.enum.Level

data class RecommendPlaceResponse (
    val level: Level,
    val imageUrl: String?,
    val displayTitle: String,
    val placeName: String,
    val area: String,
    val displayTags: List<String>
)