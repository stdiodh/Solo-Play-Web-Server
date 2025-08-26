package com.example.solo_play_web_server.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class Member(
    @Id
    val id : String? = null,

    val email : String,

    val password: String,
)
