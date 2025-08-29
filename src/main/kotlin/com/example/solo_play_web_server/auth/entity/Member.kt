package com.example.solo_play_web_server.auth.entity

import com.example.solo_play_web_server.auth.dto.Agreement
import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class Member(
    @Id
    val id : String? = null,
    val email : String,
    val password : String,
    val nickname : String,
    val imageUrl : String? = null,
    val provider : AuthProvider,
    val role : Set<MemberRole>,
    val agreement : Agreement,
    val verified: Boolean
)