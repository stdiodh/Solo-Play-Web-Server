package com.example.solo_play_web_server.place.dto

import com.example.solo_play_web_server.place.entity.KakaoPlace
import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoApiResponse(
    @JsonProperty("documents")
    val documents: List<KakaoPlace>,
    @JsonProperty("meta")
    val meta: Meta
)

data class Meta(
    //마지막 페이지인지 확인
    @JsonProperty("is_end")
    val isEnd: Boolean,
    //카테고리별 총 장소의 개수
    @JsonProperty("total_count")
    val totalCount: Int,
)