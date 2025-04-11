package com.example.solo_play_web_server.member.entity

import com.example.solo_play_web_server.member.enums.Gender
import com.example.solo_play_web_server.member.enums.MemberRole
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Member")
class Member (
    @Id
    val id : String? = null,
    val email : String,
    val password : String,
    val nickname : String,
    val gender : Gender,
    val birthday : String,
    val role : MemberRole,
    val imageUrl : String,
    )