package com.example.solo_play_web_server.course.service

import com.example.solo_play_web_server.course.dtos.CourseRequestDto
import com.example.solo_play_web_server.course.dtos.CourseResponseDto
import com.example.solo_play_web_server.course.dtos.CourseWithPlacesResponseDto
import com.example.solo_play_web_server.course.entity.CoursePlace
import com.example.solo_play_web_server.course.repository.CoursePlaceRepository
import com.example.solo_play_web_server.course.repository.CourseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CourseService @Autowired constructor(
    private val courseRepository: CourseRepository,
    private val coursePlaceRepository: CoursePlaceRepository
) {

    /**
     * 모든 코스를 불러오는 메서드
     * @return List<CourseResponseDto> - 모든 코스 리스트 반환
     */
    @Transactional
    suspend fun getAllCourses(): List<CourseResponseDto> {
        val courses = courseRepository.findAllCourses()
        return courses.map { course ->
            val placeIds = coursePlaceRepository.findPlacesByCourseId(course.id!!)
            course.toResponse(placeIds)
        }
    }

    /**
     * 특정 ID의 코스를 조회하는 메서드
     * @param id 코스 ID
     * @return CourseResponseDto? - 해당 ID의 코스 데이터 (없을 경우 null)
     */
    @Transactional
    suspend fun getCourseById(id: Long): CourseResponseDto? {
        val course = courseRepository.findById(id) ?: return null
        val placeIds = coursePlaceRepository.findPlacesByCourseId(id)
        return course.toResponse(placeIds)
    }

    /**
     * 새로운 코스를 생성하는 메서드
     * @param courseRequestDto 코스 생성 요청 데이터
     * @return CourseResponseDto - 생성된 코스 데이터
     */
    @Transactional
    suspend fun createCourse(courseRequestDto: CourseRequestDto): CourseResponseDto {
        // 'place' 필드는 제외하고 course 생성
        val course = courseRepository.save(courseRequestDto.toEntity())

        // course_place 테이블에 관련 장소 ID 저장
        courseRequestDto.places.forEach { placeId ->
            coursePlaceRepository.save(CoursePlace(courseId = course.id!!, placeId = placeId))
        }

        return course.toResponse(courseRequestDto.places)
    }

    /**
     * 특정 코스를 장소와 함께 조회하는 메서드
     * @param courseId 코스 ID
     * @return CourseWithPlacesResponseDto - 장소 목록을 포함한 코스 데이터
     */
    @Transactional
    suspend fun getCourseWithPlaces(courseId: Long): CourseWithPlacesResponseDto {
        val course = courseRepository.findById(courseId) ?: throw Exception("코스를 찾을 수 없습니다.")
        val placeIds = coursePlaceRepository.findPlacesByCourseId(courseId)

        return CourseWithPlacesResponseDto(
            id = course.id!!,
            userId = course.userId,
            createAt = course.createAt,
            like = course.like,
            region = course.region,
            category = course.category,
            title = course.title,
            content = course.content,
            places = placeIds,
            post = course.post,
            review = course.review
        )
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

        // 기존 장소 데이터 삭제
        coursePlaceRepository.deleteByCourseId(id)

        // 새로운 장소 ID 저장
        courseRequestDto.places.forEach { placeId ->
            coursePlaceRepository.save(CoursePlace(courseId = id, placeId = placeId))
        }

        // 코스 정보 업데이트
        val updatedCourse = courseRequestDto.toEntity().copy(id = existingCourse.id)
        val savedCourse = courseRepository.save(updatedCourse)

        return savedCourse.toResponse(courseRequestDto.places)
    }

    /**
     * 특정 코스를 삭제하는 메서드
     * @param id 코스 ID
     * @return String - 삭제 결과 메시지 반환
     */
    @Transactional
    suspend fun deleteCourse(id: Long): String {
        val course = courseRepository.findById(id) ?: return "ID $id 에 해당하는 코스를 찾을 수 없습니다."

        // 중간 테이블에서 해당 코스와 연관된 장소 데이터 삭제
        coursePlaceRepository.deleteByCourseId(id)

        // 코스 삭제
        courseRepository.deleteById(id)

        return "코스가 성공적으로 삭제되었습니다."
    }

    /**
     * 상위 10위 추천 코스 리스트를 반환하는 메서드
     * @return List<CourseResponseDto> - 추천 코스 상위 10개 리스트
     */
    suspend fun getTop10RecommendedCourses(): List<CourseResponseDto> {
        // 추천 코스 상위 10개 조회
        val courses = courseRepository.findTop10RecommendedCourses()

        // 각 코스를 CourseResponseDto로 변환 후 반환
        return courses.map { course ->
            // 장소 ID들을 가져와서 toResponse 메서드에 전달
            val placeIds = coursePlaceRepository.findPlacesByCourseId(course.id!!)
            course.toResponse(placeIds)
        }
    }

    /**
     * 오늘 가장 좋아요를 많이 받은 코스 리스트를 반환하는 메서드
     * @return List<CourseResponseDto> - 오늘 좋아요를 많이 받은 코스 리스트
     */
    suspend fun getTopLikedCoursesToday(): List<CourseResponseDto> {
        // 오늘 날짜를 가져옴
        val today = LocalDate.now()

        // 오늘 좋아요를 많이 받은 코스들 조회
        val courses = courseRepository.findTopLikedCoursesToday(today)

        // 각 코스를 CourseResponseDto로 변환 후 반환
        return courses.map { course ->
            // 장소 ID들을 가져와서 toResponse 메서드에 전달
            val placeIds = coursePlaceRepository.findPlacesByCourseId(course.id!!)
            course.toResponse(placeIds)
        }
    }
}
