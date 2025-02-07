package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Course
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface CourseRepository : CoroutineCrudRepository<Course, Long>{
    /**
     * 모든 코스 조회 쿼리
     */
    @Query("SELECT * FROM course")
    suspend fun findAllCourses(): List<Course>

    /**
     * 모든 코스의 좋아요 내림차순 정렬 쿼리
     */
    @Query("SELECT * FROM course ORDER BY likes_count DESC LIMIT 10")
    suspend fun findTop10RecommendedCourses(): List<Course>

    /**
     * 오늘 생성된 코스 중 좋아요 내림차순 정렬 쿼리
     */
    @Query("SELECT * FROM course WHERE create_at = :today ORDER BY likes_count DESC")
    suspend fun findTopLikedCoursesToday(today: LocalDate): List<Course>
}