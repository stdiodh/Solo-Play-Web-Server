package com.example.solo_play_web_server.course.service

import com.example.solo_play_web_server.common.exception.place.PlaceNotFoundException
import com.example.solo_play_web_server.course.dtos.CourseRequestDto
import com.example.solo_play_web_server.course.entity.Course
import com.example.solo_play_web_server.course.enums.Category
import com.example.solo_play_web_server.course.repository.CourseRepository
import com.example.solo_play_web_server.place.repository.PlaceRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val placeRepository: PlaceRepository
) {
    /**
     * 코스를 생성하는 메소드
     * 장소 ID 리스트를 확인하고, 유효한 장소들을 바탕으로 코스를 생성합니다.
     */
    suspend fun createCourse(courseRequestDto: CourseRequestDto): Mono<Course> {
        // place ID들로 Place 객체들을 조회
        return placeRepository.findAllById(courseRequestDto.place)
            .collectList()  // Place 객체들을 리스트로 받음
            .flatMap { places ->
                if (places.size != courseRequestDto.place.size) {
                    // 만약 요청된 ID 중 존재하지 않는 Place가 있다면 예외를 발생시킬 수 있음
                    return@flatMap Mono.error<Course>(PlaceNotFoundException("Some places not found"))
                }

                // Course 객체 생성 (place는 ID 목록으로 저장)
                val course = Course(
                    userId = courseRequestDto.userId,
                    title = courseRequestDto.title,
                    content = courseRequestDto.content,
                    region = courseRequestDto.region,
                    category = courseRequestDto.category,
                    place = courseRequestDto.place,  // place는 ID 목록으로 저장
                    post = courseRequestDto.post,
                    review = courseRequestDto.review,
                    level = courseRequestDto.level
                )

                // Course 저장
                courseRepository.save(course)
            }
    }

    // 코스 전체 조회
    suspend fun getAllCourses(): Flux<Course> {
        return courseRepository.findAll()
    }

    // 특정 ID의 코스 조회
    suspend fun getCourseById(id: String): Mono<Course> {
        return courseRepository.findById(id)
    }

    // 유저 ID로 코스 조회
    suspend fun getCoursesByUserId(userId: String): Flux<Course> {
        return courseRepository.findByUserId(userId)
    }

    // 카테고리로 코스 조회
    suspend fun getCoursesByCategory(category: Category): Flux<Course> {
        return courseRepository.findByCategory(category.name)
    }

    // 코스 수정
    suspend fun updateCourse(id: String, courseRequestDto: CourseRequestDto): Mono<Course> {
        return courseRepository.findById(id)
            .flatMap { existingCourse ->
                // 장소 ID들로 Place 객체들을 조회
                placeRepository.findAllById(courseRequestDto.place)
                    .collectList() // 리스트로 변환
                    .flatMap { places ->
                        // 장소가 하나라도 없으면 예외를 던짐
                        if (places.size != courseRequestDto.place.size) {
                            return@flatMap Mono.error<Course>(PlaceNotFoundException("Some places not found"))
                        }

                        val updatedCourse = existingCourse.copy(
                            title = courseRequestDto.title,
                            content = courseRequestDto.content,
                            region = courseRequestDto.region,
                            level = courseRequestDto.level,
                            category = courseRequestDto.category,
                            place = courseRequestDto.place, // 업데이트된 장소 ID 목록으로 변경
                            post = courseRequestDto.post,
                            review = courseRequestDto.review
                        )
                        courseRepository.save(updatedCourse) // 업데이트된 코스 저장
                    }
            }
    }

    // 코스 삭제
    suspend fun deleteCourse(id: String): Mono<Void> {
        return courseRepository.deleteById(id)
    }
}
