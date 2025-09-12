package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.entity.Member
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface MemberRepository : ReactiveMongoRepository<Member, String?>{
    fun existsByEmail(email: String) : Mono<Boolean>
    fun findByEmail(email: String) : Mono<Member>
}