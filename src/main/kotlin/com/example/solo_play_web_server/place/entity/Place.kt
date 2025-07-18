package com.example.solo_play_web_server.place.entity

import com.example.solo_play_web_server.common.dto.Level
import com.example.solo_play_web_server.place.enums.Area
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "place")
data class Place(
    @Id
    val id: String? = null,
    val name: String,
    val description: String,
    val region: Area,
    val level : Level,
    val tags : List<String>,
    val urls: List<String>
)