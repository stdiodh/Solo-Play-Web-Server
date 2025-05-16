package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.Course
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface CourseRepository : ReactiveMongoRepository<Course, String>{
    suspend fun findByUserId(userId: String): Flux<Course>
    suspend fun findByCourseCategory(courseCategory: String): Flux<Course>
}