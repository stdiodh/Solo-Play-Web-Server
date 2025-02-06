package com.example.solo_play_web_server.course.dtos

import com.example.solo_play_web_server.common.annotation.ValidEnum
import com.example.solo_play_web_server.course.entity.Course
import com.example.solo_play_web_server.course.enums.Region
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CourseRequestDto(
    @JsonProperty("userId")
    private var _userId: Long,

    @JsonProperty("createAt")
    private var _createAt: String,

    @JsonProperty("likesCount")
    private var _like: Int,

    @field:ValidEnum(enumClass = Region::class, message = "잘못된 지역 입니다.")
    @JsonProperty("region")
    private var _region: String,

    @JsonProperty("category")
    private var _category: String,

    @JsonProperty("title")
    private var _title: String,

    @JsonProperty("content")
    private var _content: String,

    @JsonProperty("places")
    private var _places: List<Long>,

    @JsonProperty("post")
    private var _post: Long,

    @JsonProperty("review")
    private var _review: Long
) {
    val userId: Long
        get() = _userId

    val createAt: LocalDate
        get() = _createAt.toLocalDate()

    val like: Int
        get() = _like

    val region: Region
        get() = Region.valueOf(_region!!)

    val category: String
        get() = _category

    val title: String
        get() = _title

    val content: String
        get() = _content

    val places: List<Long>
        get() = _places

    val post: Long
        get() = _post

    val review: Long
        get() = _review

    fun toEntity(): Course = Course(
        userId = userId,
        createAt = createAt,
        like = like,
        region = region,
        category = category,
        title = title,
        content = content,
        post = post,
        review = review
    )

    private fun String.toLocalDate(): LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

data class CourseResponseDto(
    val id: Long,
    val userId: Long,
    val createAt: LocalDate,
    val like: Int,
    val region: Region,
    val category: String,
    val title: String,
    val content: String,
    val places: List<Long>,
    val post: Long,
    val review: Long
)

data class CourseWithPlacesResponseDto(
    val id: Long,
    val userId: Long,
    val createAt: LocalDate,
    val like: Int,
    val region: Region,
    val category: String,
    val title: String,
    val content: String,
    val places: List<Long>,  // 장소 ID 리스트 포함
    val post: Long,
    val review: Long
)