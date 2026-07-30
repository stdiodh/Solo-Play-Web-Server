package com.example.solo_play_web_server.place.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoPlace(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("place_name")
    val placeName: String,
    @JsonProperty("category_name")
    val categoryName: String,
    @JsonProperty("address_name")
    val addressName: String,
    @JsonProperty("road_address_name")
    val roadAddressName: String,
    @JsonProperty("place_url")
    val placeUrl: String
)