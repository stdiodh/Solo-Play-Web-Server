package com.example.solo_play_web_server.course.controller

import com.example.solo_play_web_server.common.dtos.BaseResponse
import com.example.solo_play_web_server.course.dtos.CourseRequestDto
import com.example.solo_play_web_server.course.dtos.CourseResponseDto
import com.example.solo_play_web_server.course.service.CourseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Course API", description = "코스 관리 API")
@RestController
@RequestMapping("/api/course")
class CourseController(
    private val courseService: CourseService
) {

    /**
     * 모든 코스를 불러오는 API
     * @return ResponseEntity<List<CourseResponseDto>> - 모든 코스 데이터 리스트 반환
     */
    @Operation(summary = "모든 코스 조회", description = "등록된 모든 코스를 조회합니다.")
    @GetMapping
    suspend fun getAllCourses(): ResponseEntity<BaseResponse<List<CourseResponseDto>>> {
        val result = courseService.getAllCourses()
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse(result))
    }

    /**
     * 특정 코스를 ID로 조회하는 API
     * @param id 코스 ID
     * @return ResponseEntity<CourseResponseDto> - 해당 ID의 코스 데이터 반환
     */
    @Operation(summary = "코스 상세 조회", description = "특정 ID를 가진 코스의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    suspend fun getCourseById(@PathVariable id: Long): ResponseEntity<BaseResponse<CourseResponseDto>> {
        val result = courseService.getCourseById(id)
        return result?.let { ResponseEntity.ok(BaseResponse(it)) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse(null))
    }

    /**
     * 코스를 생성하는 API
     * @param courseRequestDto 코스 생성 요청 데이터
     * @return ResponseEntity<CourseResponseDto> - 생성된 코스 데이터 반환
     */
    @Operation(summary = "코스 생성", description = "새로운 코스를 생성합니다.")
    @PostMapping
    suspend fun createCourse(@RequestBody @Valid courseRequestDto: CourseRequestDto):
            ResponseEntity<BaseResponse<CourseResponseDto>> {
        val result = courseService.createCourse(courseRequestDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse(result))
    }

    /**
     * 코스를 수정하는 API
     * @param id 코스 ID
     * @param courseRequestDto 수정할 코스 데이터
     * @return ResponseEntity<CourseResponseDto> - 수정된 코스 데이터 반환
     */
    @Operation(summary = "코스 수정", description = "기존 코스를 수정합니다.")
    @PutMapping("/{id}")
    suspend fun updateCourse(
        @PathVariable id: Long,
        @RequestBody @Valid courseRequestDto: CourseRequestDto
    ): ResponseEntity<BaseResponse<CourseResponseDto>> {
        val result = courseService.updateCourse(id, courseRequestDto)
        return result?.let { ResponseEntity.ok(BaseResponse(it)) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse(null))
    }

    /**
     * 코스를 삭제하는 API
     * @param id 코스 ID
     * @return ResponseEntity<BaseResponse<String>> - 삭제 결과 메시지 반환
     */
    @Operation(summary = "코스 삭제", description = "ID를 이용해 코스를 삭제합니다.")
    @DeleteMapping("/{id}")
    suspend fun deleteCourse(@PathVariable id: Long): ResponseEntity<BaseResponse<String>> {
        val result = courseService.deleteCourse(id)
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse(result))
    }

    /**
     * 상위 10위 추천 코스를 조회하는 API
     * @return ResponseEntity<BaseResponse<List<CourseResponseDto>>> - 추천 코스 상위 10개 리스트 반환
     */
    @Operation(summary = "상위 10위 추천 코스 조회", description = "추천 코스 상위 10개를 조회합니다.")
    @GetMapping("/recommended")
    suspend fun getTop10RecommendedCourses(): ResponseEntity<BaseResponse<List<CourseResponseDto>>> {
        val result = courseService.getTop10RecommendedCourses()
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse(result))
    }

    /**
     * 오늘 가장 좋아요를 많이 받은 코스를 조회하는 API
     * @return ResponseEntity<BaseResponse<List<CourseResponseDto>>> - 오늘 가장 좋아요를 많이 받은 코스 리스트 반환
     */
    @Operation(summary = "오늘 가장 좋아요를 많이 받은 코스 조회", description = "오늘 좋아요를 많이 받은 코스들을 조회합니다.")
    @GetMapping("/top-liked-today")
    suspend fun getTopLikedCoursesToday(): ResponseEntity<BaseResponse<List<CourseResponseDto>>> {
        val result = courseService.getTopLikedCoursesToday()
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse(result))
    }
}