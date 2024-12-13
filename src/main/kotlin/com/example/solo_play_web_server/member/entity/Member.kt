package com.example.solo_play_web_server.member.entity

import com.example.solo_play_web_server.common.enums.Gender
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("member")
data class Member (
    @Id
    var id : Long? = null,

    @Column
    var email : String,

    @Column
    var password : String,

    @Column("nick_name")
    var nickName : String,

    @Column
    var gender : Gender,

    @Column
    var birthday : LocalDate,

    @Column("image_url")
    var imageUrl : String,
)