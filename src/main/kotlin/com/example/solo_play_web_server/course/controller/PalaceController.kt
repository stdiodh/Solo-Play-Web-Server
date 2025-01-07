package com.example.solo_play_web_server.course.controller

import com.example.solo_play_web_server.course.dtos.PalaceRequestDto
import com.example.solo_play_web_server.course.dtos.PalaceResponseDto
import com.example.solo_play_web_server.course.service.PalaceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Course 서버", description = "코스 Api")
@RestController
@RequestMapping("/api/course")
class PalaceController(
    private val palaceService: PalaceService
){
    /**
     * 코스 불러오기 Api
     */
    @Tag(name = "코스를 불러오는 Api")
    @GetMapping
    private suspend fun getMyCourse() : ResponseEntity<List<PalaceResponseDto>> {
        val result = palaceService.getMyCourse()
        return ResponseEntity.status(HttpStatus.OK).body(result)
    }

    /**
     * 코스 추가하기 Api
     */
    @Operation(description = "코스 생성 Api")
    @PostMapping
    private suspend fun createCourse(@RequestBody palaceRequestDto: PalaceRequestDto)
    : ResponseEntity<PalaceResponseDto>
    {
        val result = palaceService.savePalace(palaceRequestDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }
}