package com.example.solo_play_web_server.common.dto

data class ApiResponse<T>(
    // 성공 실패 여부
    val status: ResultStatus,
    // 여기에 메시지 성공 시 서버에서 보낼 메시지 또는 실패시 서버에서 발생한 오류 원인
    val message: String? = null,
    // 여기에 필요한 데이터 성공시 서버 반환 데이터 실패시 경우에 따라서 필요한 데이터
    val data: T? = null
)