package com.example.solo_play_web_server.member.service

import com.example.solo_play_web_server.member.dto.SignUpDto
import com.example.solo_play_web_server.member.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
){
    fun signup(signUpDto : SignUpDto) : String {
        val member = memberRepository.findByEmail(signUpDto.email)

        if(member != null){
            throw RuntimeException("이미 가입한 회원입니다!")
        }
        memberRepository.save(signUpDto.toMember())
        return "회원가입이 완료되었습니다!"
    }
}