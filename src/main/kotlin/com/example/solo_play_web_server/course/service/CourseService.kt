package com.example.solo_play_web_server.course.service

import com.example.solo_play_web_server.course.dtos.CourseRequestDto
import com.example.solo_play_web_server.course.dtos.CourseResponseDto
import com.example.solo_play_web_server.course.repository.CourseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService @Autowired constructor(
    private val courseRepository: CourseRepository
) {

    /**
     * 모든 코스를 불러오는 메서드
     * @return List<CourseResponseDto> - 모든 코스 리스트 반환
     */
    @Transactional
    suspend fun getAllCourses(): List<CourseResponseDto> {
        val courses = courseRepository.findAllCourses()
        return courses.map { it.toResponse() }
    }

    /**
     * 특정 ID의 코스를 조회하는 메서드
     * @param id 코스 ID
     * @return CourseResponseDto? - 해당 ID의 코스 데이터 (없을 경우 null)
     */
    @Transactional
    suspend fun getCourseById(id: Long): CourseResponseDto? {
        val course = courseRepository.findById(id)
        return course?.toResponse()
    }

    /**
     * 새로운 코스를 생성하는 메서드
     * @param courseRequestDto 코스 생성 요청 데이터
     * @return CourseResponseDto - 생성된 코스 데이터
     */
    @Transactional
    suspend fun createCourse(courseRequestDto: CourseRequestDto): CourseResponseDto {
        val course = courseRequestDto.toEntity()
        val savedCourse = courseRepository.save(course)
        return savedCourse.toResponse()
    }

    /**
     * 코스를 수정하는 메서드
     * @param id 코스 ID
     * @param courseRequestDto 수정할 코스 데이터
     * @return CourseResponseDto? - 수정된 코스 데이터 (없을 경우 null)
     */
    @Transactional
    suspend fun updateCourse(id: Long, courseRequestDto: CourseRequestDto): CourseResponseDto? {
        val existingCourse = courseRepository.findById(id) ?: return null
        val updatedCourse = courseRequestDto.toEntity().copy(id = existingCourse.id)
        val savedCourse = courseRepository.save(updatedCourse)
        return savedCourse.toResponse()
    }

    /**
     * 특정 코스를 삭제하는 메서드
     * @param id 코스 ID
     * @return String - 삭제 결과 메시지 반환
     */
    @Transactional
    suspend fun deleteCourse(id: Long): String {
        val course = courseRepository.findById(id) ?: return "ID $id 에 해당하는 코스를 찾을 수 없습니다."
        courseRepository.deleteById(id)
        return "코스가 성공적으로 삭제되었습니다."
    }
}
