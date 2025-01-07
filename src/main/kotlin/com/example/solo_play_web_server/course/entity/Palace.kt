package com.example.solo_play_web_server.course.entity

import com.example.solo_play_web_server.course.dtos.PalaceResponseDto
import com.example.solo_play_web_server.course.enums.Area
import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("palace")
data class Palace(
    @Id
    var id : Long? = null,

    @Column
    var area : Area,

    @Column
    var address : String,

    @Column
    var description : String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column("create_at")
    var createAt : LocalDate,

    @Column
    var urls : String
) {
    fun toResponse() : PalaceResponseDto = PalaceResponseDto(
        id = id,
        area = area,
        address = address,
        description = description,
        createAt = createAt,
        urls = urls
    )
}
