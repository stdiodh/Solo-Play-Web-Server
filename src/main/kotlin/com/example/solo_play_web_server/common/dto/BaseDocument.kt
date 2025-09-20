package com.example.solo_play_web_server.common.dto

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

abstract class BaseDocument {
    @CreatedDate
    @Field("created_at")
    var createAt: LocalDateTime? = null
        private set

    @LastModifiedDate
    @Field("updated_at")
    var updateAt: LocalDateTime? = null
        private set
}