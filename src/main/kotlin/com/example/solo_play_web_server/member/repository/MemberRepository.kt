package com.example.solo_play_web_server.member.repository

import com.example.solo_play_web_server.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String) : Member?
}