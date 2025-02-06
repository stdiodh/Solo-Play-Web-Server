package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Course
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CourseRepository : CoroutineCrudRepository<Course, Long>{
    @Query("SELECT * FROM course")
    suspend fun findAllCourses(): List<Course>

    @Query("SELECT * FROM course ORDER BY likes_count DESC LIMIT 10")
    suspend fun findTop10RecommendedCourses(): List<Course>

    @Query("SELECT * FROM course WHERE create_at = :today ORDER BY likes_count DESC")
    suspend fun findTopLikedCoursesToday(today: LocalDate): List<Course>
}