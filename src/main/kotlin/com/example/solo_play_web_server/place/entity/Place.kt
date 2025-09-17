package com.example.solo_play_web_server.place.entity

import com.example.solo_play_web_server.common.dto.BaseDocument
import com.example.solo_play_web_server.place.enum.MainCategory
import com.example.solo_play_web_server.place.enums.SubCategory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("places")
class Place (
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val kakaoPlaceId: String,

    @Field("placeName")
    val placeName: String,

    @Field("area")
    val area: String,

    @Field("address")
    val address: String,

    @Field("description")
    val description: String,

    @Field("main_category")
    val mainCategory: MainCategory,

    @Field("sub_category")
    val subCategory: SubCategory,

    @Field("level")
    val level: Int,

    @Field("kakao_category_name")
    val kakaoCategoryName: String,

    @Field("display_title")
    val displayTitle: String,

    @Field("display_tags")
    val displayTags: List<String?> = listOf(),

    @Field("urls")
    val urls: List<String> = listOf()
): BaseDocument()