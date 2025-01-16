package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Course
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : CoroutineCrudRepository<Course, Long>{
    @Query("SELECT * FROM course")
    suspend fun findAllCourses(): List<Course>
}