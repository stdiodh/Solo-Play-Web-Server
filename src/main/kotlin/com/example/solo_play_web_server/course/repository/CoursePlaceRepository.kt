package com.example.solo_play_web_server.course.repository

import com.example.solo_play_web_server.course.entity.CoursePlace
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CoursePlaceRepository : CoroutineCrudRepository<CoursePlace, Long> {
    /**
     * 코스에 연결된 장소를 가져오는 쿼리
     */
    @Query("SELECT place_id FROM course_place WHERE course_id = :courseId")
    suspend fun findPlacesByCourseId(courseId: Long): List<Long>

    /**
     * 코스를 삭제할 때 연결된 장소들을 제거하기 위한 쿼리
     */
    @Query("DELETE FROM course_place WHERE course_id = :courseId")
    suspend fun deleteByCourseId(courseId: Long)
}
