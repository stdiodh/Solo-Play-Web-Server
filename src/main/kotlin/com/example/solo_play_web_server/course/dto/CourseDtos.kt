package com.example.solo_play_web_server.course.dto

import com.example.solo_play_web_server.course.enums.CourseCategory
import com.example.solo_play_web_server.common.annotation.ValidEnum
import com.example.solo_play_web_server.course.enums.Level
import com.example.solo_play_web_server.place.enums.Region
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CourseRequestDto(
    @field:NotBlank(message = "사용자 ID는 필수입니다.")
    val userId: String,

    @field:NotBlank(message = "제목은 필수입니다.")
    val title: String,

    @field:NotBlank(message = "내용은 필수입니다.")
    val content: String,

    @field:NotNull(message = "레벨 선택은 필수입니다.")
    @field:ValidEnum(enumClass = Level::class, message = "유효한 레벨을 선택하세요.")
    val level: Level,

    @field:NotNull(message = "지역은 필수입니다.")
    @field:ValidEnum(enumClass = Region::class, message = "유효한 지역을 선택하세요.")
    val region: Region,

    @field:NotNull(message = "카테고리는 필수입니다.")
    @field:ValidEnum(enumClass = CourseCategory::class, message = "유효한 카테고리를 선택하세요.")
    val courseCategory: CourseCategory,

    @field:NotNull(message = "장소 ID는 필수입니다.")
    val place: List<String>,

    val post: List<String>?,
    val review: List<String>?
)