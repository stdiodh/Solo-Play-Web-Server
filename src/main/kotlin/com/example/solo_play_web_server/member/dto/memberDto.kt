package com.example.solo_play_web_server.member.dto

import com.example.solo_play_web_server.common.annotation.ValidEnum
import com.example.solo_play_web_server.common.enums.Gender
import com.example.solo_play_web_server.member.entity.Member
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SignUpDto (
    @field:NotBlank(message = "이메일을 입력하세요!")
    @field:Email(message = "올바르지 못한 이메일 형식입니다!")
    @JsonProperty("email")
    private val _email : String?,

    @field:NotBlank(message = "비밀번호를 입력하세요!")
    @field:Pattern(regexp ="^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*()])[a-zA-Z0-9!@#\$%^&*()]{8,20}\$",
        message = "올바르지 못한 비밀번호 형식입니다!")
    @JsonProperty("password")
    private val _password : String?,

    @field:NotBlank(message = "닉네임을 입력하세요!")
    @JsonProperty("nickName")
    private val _nickName : String,

    @field:NotBlank(message = "성별을 입력하세요!")
    @field:ValidEnum(enumClass = Gender::class, message = "성별은 MAN 혹은 WOMAN입니다!")
    @JsonProperty("gender")
    private val _gender : String?,

    @field:NotBlank(message = "생년월일을 입력하세요!")
    @field:Pattern(regexp = "^([12]\\d{3})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])\$",
        message = "올바르지 못한 날짜 방식입니다!")
    @JsonProperty("birthday")
    private val _birthday : String,

    @field:NotBlank(message = "이미지 Url을 입력하세요!")
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