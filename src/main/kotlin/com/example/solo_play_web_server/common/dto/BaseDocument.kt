package com.example.solo_play_web_server.common.dto

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

abstract class BaseDocument {
    @CreatedDate
    lateinit var createAt: LocalDateTime
        private set

    @LastModifiedDate
    lateinit var updateAt: LocalDateTime
        private set
}