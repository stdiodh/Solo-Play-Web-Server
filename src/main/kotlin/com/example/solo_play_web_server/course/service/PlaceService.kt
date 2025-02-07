package com.example.solo_play_web_server.course.service

import com.example.solo_play_web_server.course.dtos.PlaceRequestDto
import com.example.solo_play_web_server.course.entity.Place
import com.example.solo_play_web_server.course.enums.Region
import com.example.solo_play_web_server.course.repository.PlaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceService (
    private val placeRepository: PlaceRepository,
){
     /**
      * 특정 지역에 장소 생성
     */
    @Transactional
    suspend fun createPlace(requestDto : PlaceRequestDto): Place{
        val place = requestDto.toEntity()
        return placeRepository.save(place)
    }

    /**
     * 특정 지역의 장소 가져오기
     */
    @Transactional
    suspend fun getPlacesByRegion(region: Region): List<Place>{
        return placeRepository.findByRegion(region.name)
    }
}