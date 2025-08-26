package com.example.solo_play_web_server.auth.repository

import com.example.solo_play_web_server.auth.entity.Member
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface MemberRepository : ReactiveMongoRepository<Member, String?>