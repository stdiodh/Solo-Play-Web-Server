package com.example.solo_play_web_server.course.entity

import com.example.solo_play_web_server.course.enums.Category
import com.example.solo_play_web_server.course.enums.Level
import com.example.solo_play_web_server.place.enums.Region
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "course")
data class Course(
    @Id
    val id: String? = null,
    val userId: String,
    val title: String,
    val content: String,
    val region: Region,
    val level: Level,
    val category: Category,
    val place: List<String>,
    val post: List<String>?,
    val review: List<String>?,
    val saved: Int = 0,
    val createAt: LocalDateTime = LocalDateTime.now()
)
