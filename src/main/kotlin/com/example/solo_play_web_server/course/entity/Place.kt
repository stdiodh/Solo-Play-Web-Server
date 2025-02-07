package com.example.solo_play_web_server.course.entity

import com.example.solo_play_web_server.course.enums.Region
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("place")
data class Place (
    @Id
    val id : Long? = null,

    @Column
    val name : String,

    @Column
    val region : Region,

    @Column
    val description : String,

    @Column
    val urls : String,
)