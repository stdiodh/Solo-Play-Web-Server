package com.example.solo_play_web_server.member.entity

import com.example.solo_play_web_server.common.enums.Gender
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Member (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id : Long?,

    @Column(nullable = false, updatable = false, length = 100)
    var email : String,

    @Column(nullable = false, length = 100)
    var password : String,

    @Column(nullable = false, length = 30)
    var nickName : String,

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    var gender : Gender,

    @Column(nullable = false, length = 30)
    var birthday : LocalDate,

    @Column(nullable = false, length = 1000)
    var imageUrl : String,
)