package com.example.solo_play_web_server.place.entity

import com.example.solo_play_web_server.place.enums.Region
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "place")
data class Place(
    @Id
    val id: String? = null,
    val name: String,
    val region: Region,
    val description: String,
    val saved: Int = 0,
    val urls: List<String>
)