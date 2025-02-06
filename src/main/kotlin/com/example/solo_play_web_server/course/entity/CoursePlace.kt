package com.example.solo_play_web_server.course.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("course_place")
data class CoursePlace(
    @Column("course_id")
    val courseId: Long,

    @Column("place_id")
    val placeId: Long
)