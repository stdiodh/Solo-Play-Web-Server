package com.example.solo_play_web_server.course.controller

import com.example.solo_play_web_server.common.dtos.BaseResponse
import com.example.solo_play_web_server.common.enum.ResultStatus
import com.example.solo_play_web_server.common.exception.CommonNotFoundException
import com.example.solo_play_web_server.course.dtos.CourseRequestDto
import com.example.solo_play_web_server.course.entity.Course
import com.example.solo_play_web_server.course.enums.Category
import com.example.solo_play_web_server.course.service.CourseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Tag(name = "Course API", description = "코스 관리 API")
@RestController
@RequestMapping("/api/course")
class CourseController(
    private val courseService: CourseService
) {
    @Operation(summary = "코스 생성", description = "코스를 생성합니다. 장소 ID가 유효해야 합니다.")
    @PostMapping
    suspend fun createCourse(@Valid @RequestBody courseRequestDto: CourseRequestDto): Mono<BaseResponse<Course>> {
        return try {
            courseService.createCourse(courseRequestDto)
                .map { course ->
                    BaseResponse(data = course)
                }
        } catch (e: CommonNotFoundException) {
            Mono.just(
                BaseResponse(
                    status = ResultStatus.ERROR.name,
                    resultMsg = e.message ?: "장소 ID가 유효하지 않습니다."
                )
            )
        }
    }

    // 모든 코스 조회
    @Operation(summary = "모든 코스 조회", description = "모든 코스를 조회합니다.")
    @GetMapping
    private suspend fun getAllCourses(): Flux<BaseResponse<Course>> {
        return courseService.getAllCourses()
            .map { course -> BaseResponse(data = course) }
    }

    // 특정 ID의 코스 조회
    @Operation(summary = "코스 조회", description = "특정 ID의 코스를 조회합니다.")
    @GetMapping("/{id}")
    private suspend fun getCourseById(@PathVariable id: String): Mono<BaseResponse<Course>> {
        return courseService.getCourseById(id)
            .map { course -> BaseResponse(data = course) }
    }

    // 유저 ID로 코스 조회
    @Operation(summary = "유저별 코스 조회", description = "특정 유저의 코스를 조회합니다.")
    @GetMapping("/user/{userId}")
    private suspend fun getCoursesByUserId(@PathVariable userId: String): Flux<BaseResponse<Course>> {
        return courseService.getCoursesByUserId(userId)
            .map { course -> BaseResponse(data = course) }
    }

    // 카테고리로 코스 조회
    @Operation(summary = "카테고리별 코스 조회", description = "특정 카테고리의 코스를 조회합니다.")
    @GetMapping("/category/{category}")
    private suspend fun getCoursesByCategory(@PathVariable category: Category): Flux<BaseResponse<Course>> {
        return courseService.getCoursesByCategory(category)
            .map { course -> BaseResponse(data = course) }
    }

    // 코스 수정
    @Operation(summary = "코스 수정", description = "특정 코스를 수정합니다.")
    @PutMapping("/{id}")
    private suspend fun updateCourse(
        @PathVariable id: String,
        @Valid @RequestBody courseRequestDto: CourseRequestDto
    ): Mono<BaseResponse<Course>> {
        return courseService.updateCourse(id, courseRequestDto)
            .map { course -> BaseResponse(data = course) }
    }

    // 코스 삭제
    @Operation(summary = "코스 삭제", description = "특정 코스를 삭제합니다.")
    @DeleteMapping("/{id}")
    private suspend fun deleteCourse(@PathVariable id: String): Mono<BaseResponse<String>> {
        return courseService.deleteCourse(id)
            .then(Mono.just(BaseResponse(data = "코스 삭제 완료")))
    }

    // 저장됨 수 증가
    @Operation(summary = "코스 저장됨 수 증가", description = "특정 코스의 저장됨 수를 증가시킵니다.")
    @PostMapping("/{courseId}/saved")
    private suspend fun incrementSaved(
        @Parameter(description = "코스 ID") @PathVariable courseId: String
    ): Mono<BaseResponse<Course>> {
        return courseService.incrementSaved(courseId)
            .map { course -> BaseResponse(data = course) }
            .onErrorResume { e ->
                Mono.just(BaseResponse(status = "ERROR", resultMsg = e.message ?: "An error occurred"))
            }
    }

    @Operation(summary = "저장됨 순위 상위 10개 코스 조회", description = "저장됨 수 기준으로 상위 10개 코스를 조회합니다.")
    @GetMapping("/top10/saved")
    private suspend fun getTop10CoursesBySaved(): Flux<BaseResponse<Course>> {
        return courseService.getTop10CoursesBySaved()
            .map { course -> BaseResponse(data = course) }
    }
}