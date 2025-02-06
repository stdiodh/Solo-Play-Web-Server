package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.CoursePlace
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.transaction.annotation.Transactional

interface CoursePlaceRepository : CoroutineCrudRepository<CoursePlace, Long> {

    @Query("SELECT place_id FROM course_place WHERE course_id = :courseId")
    suspend fun findPlacesByCourseId(courseId: Long): List<Long>

    @Query("DELETE FROM course_place WHERE course_id = :courseId")
    @Transactional
    suspend fun deleteByCourseId(courseId: Long)
}
