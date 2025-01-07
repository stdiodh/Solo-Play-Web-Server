package com.example.solo_play_web_server.course.dtos

import com.example.solo_play_web_server.common.annotation.ValidEnum
import com.example.solo_play_web_server.course.entity.Palace
import com.example.solo_play_web_server.course.enums.Area
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class PalaceRequestDto (
    @JsonProperty("id")
    private var _id : Long? = null,

    @field:ValidEnum(enumClass = Area::class, message = "잘못된 지역 입니다.")
    @JsonProperty("area")
    private var _area : String?,

    @JsonProperty("address")
    private var _address : String?,

    @JsonProperty("description")
    private var _description : String?,

    @JsonProperty("createAt")
    private var _createAt : String?,

    @JsonProperty("urls")
    private var _urls : String,
) {
    val id : Long?
        get() = _id
    val area : Area
        get() = Area.valueOf(_area!!)
    val address : String
        get() = _address!!
    val description : String
        get() = _description!!
    val createAt : LocalDate
        get() = _createAt!!.toLocalDate()
    val urls : String
        get() = _urls

    fun toEntity() : Palace {
        val jsonUrls = jacksonObjectMapper().writeValueAsString(urls.split(", ").map { it.trim() })
        return Palace(
            id = id,
            area = area,
            address = address,
            description = description,
            createAt = createAt,
            urls = jsonUrls
        )
    }

    private fun String.toLocalDate() : LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

data class PalaceResponseDto(
    val id : Long?,
    val area : Area,
    val address : String,
    val description : String,
    val createAt : LocalDate,
    val urls : String,
) {
    companion object {
        fun fromEntity(palace: Palace) : PalaceResponseDto {
            val urlsList = jacksonObjectMapper().readValue(palace.urls, List::class.java) as List<String>
            return PalaceResponseDto(
                id = palace.id,
                area = palace.area,
                address = palace.address,
                description = palace.description,
                createAt = palace.createAt,
                urls = urlsList.joinToString(", ")
            )
        }
    }
}