package com.example.solo_play_web_server.course.dtos

import com.example.solo_play_web_server.common.annotation.ValidEnum
import com.example.solo_play_web_server.course.entity.Place
import com.example.solo_play_web_server.course.enums.Region
import com.fasterxml.jackson.annotation.JsonProperty

data class PlaceRequestDto (
    @field:ValidEnum(enumClass = Region::class, message = "잘못된 지역 입니다.")
    @JsonProperty("region")
    private var _region: String,

    @JsonProperty("name")
    private var _name : String,

    @JsonProperty("description")
    private var _description : String,

    @JsonProperty("urls")
    private val _urls : String
) {
    val name : String
        get() = _name
    val region : Region
        get() = Region.valueOf(_region!!)
    val description : String
        get() = _description
    val urls : String
        get() = _urls

    fun toEntity() : Place = Place(
        name = name,
        region = region,
        description = description,
        urls = urls
    )
}