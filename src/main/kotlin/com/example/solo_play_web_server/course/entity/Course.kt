package com.example.solo_play_web_server.course.entity

import com.example.solo_play_web_server.course.dtos.CourseResponseDto
import com.example.solo_play_web_server.course.enums.Region
import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("course")
data class Course(
    @Id
    var id: Long? = null,

    @Column("user_id")
    var userId: Long,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column("create_at")
    var createAt: LocalDate,

    @Column("likes_count")
    var like: Int,

    @Column
    var region: Region,

    @Column
    var category: String,

    @Column
    var title: String,

    @Column
    var content: String,

    @Column("post")
    var post: Long,

    @Column("review")
    var review: Long
) {
    fun toResponse(placeIds: List<Long>): CourseResponseDto = CourseResponseDto(
        id = id!!,
        userId = userId,
        createAt = createAt,
        like = like,
        region = region,
        category = category,
        title = title,
        content = content,
        places = placeIds,
        post = post,
        review = review
    )
}