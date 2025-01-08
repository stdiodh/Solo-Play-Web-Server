package com.example.solo_play_web_server.common.dtos

data class BaseResponse<T> (
    val data : T? = null,
)