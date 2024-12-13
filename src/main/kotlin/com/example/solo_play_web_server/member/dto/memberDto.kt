package com.example.solo_play_web_server.member.dto

import com.example.solo_play_web_server.common.enums.Gender
import com.example.solo_play_web_server.member.entity.Member
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SignUpDto (
    @JsonProperty("email")
    private val _email : String?,

    @JsonProperty("password")
    private val _password : String?,

    @JsonProperty("nickName")
    private val _nickName : String,

    @JsonProperty("gender")
    private val _gender : String?,

    @JsonProperty("birthday")
    private val _birthday : String,

    @JsonProperty("imageUrl")
    private val _imageUrl : String,
    ){
    val email : String
        get() = _email!!
    val password : String
        get() = _password!!
    val nickName : String
        get() = _nickName
    val gender : Gender
        get() = Gender.valueOf(_gender!!)
    val birthday : LocalDate
        get() = _birthday.toLocalDate()
    val imageUrl : String
        get() = _imageUrl

    fun toMember() : Member = Member(
        id = null,
        email = email,
        password = password,
        nickName = nickName,
        gender = gender,
        birthday = birthday,
        imageUrl = imageUrl,
    )

    private fun String.toLocalDate() : LocalDate =
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

data class memberResponseDto(
    val id : Long,
    val email : String,
    val password : String,
    val nickName : String,
    val gender : Gender,
    val birthday : LocalDate,
    val imageUrl : String,
)