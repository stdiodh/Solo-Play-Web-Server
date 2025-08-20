package com.example.solo_play_web_server.auth.repositories

import com.example.solo_play_web_server.auth.entities.Member
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface MemberRepository : ReactiveMongoRepository<Member, String?>