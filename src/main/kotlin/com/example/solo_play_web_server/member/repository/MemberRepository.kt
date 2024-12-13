package com.example.solo_play_web_server.member.repository

import com.example.solo_play_web_server.member.entity.Member
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface MemberRepository : CoroutineCrudRepository<Member, Long> {
    @Query("SELECT * FROM member WHERE email = :email")
    suspend fun findByEmail(email: String) : Member?
}