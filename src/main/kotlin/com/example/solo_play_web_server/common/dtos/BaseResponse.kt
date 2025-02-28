package com.example.solo_play_web_server.common.dtos

import com.example.solo_play_web_server.common.enum.ResultStatus

data class BaseResponse<T>(
    var status : String = ResultStatus.SUCCESS.name,
    var data : T? = null,
    var resultMsg : String = ResultStatus.SUCCESS.msg,
)
