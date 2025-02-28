package com.example.solo_play_web_server.course.entity

import com.example.solo_play_web_server.course.enums.Region
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "place")
data class Place(
    @Id
    val id: String? = null,
    val name: String,
    val region: Region,
    val description: String,
    val urls: List<String>
)