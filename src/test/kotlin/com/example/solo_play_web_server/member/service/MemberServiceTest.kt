package com.example.solo_play_web_server.member.service

import com.example.solo_play_web_server.member.dto.SignUpDto
import com.example.solo_play_web_server.member.repository.MemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class MemberServiceTest{
    private val memberRepository : MemberRepository = mockk()
    private val memberService : MemberService = MemberService(memberRepository)
    @Test
    fun `회원가입에 성공하면 문자열로 회원가입에 성공했습니다가 반환된다`(){
        val signUpDto : SignUpDto = SignUpDto("test@test.com", "test1234"
            ,"test", "MAN", "2024-11-04", "test")
        every { memberRepository.findByEmail(signUpDto.email) }returns null
        every { memberRepository.save(any()) }returns signUpDto.toMember()
        val result = memberService.signup(signUpDto)

        verify(exactly = 1) { memberRepository.findByEmail(signUpDto.email) }
        verify(exactly = 1) { memberRepository.save(any()) }
        assertEquals("회원가입이 완료되었습니다!", result)
    }

    @Test
    fun `회원가입 같은 회원이 있다면 RuntimeException이 발생한다`(){
        val signUpDto : SignUpDto = SignUpDto("test@test.com", "test1234!@#"
            ,"test", "MAN", "2024-11-04", "test")
        every { memberRepository.findByEmail(signUpDto.email) }returns signUpDto.toMember()
        every { memberRepository.save(any()) }returns signUpDto.toMember()
        assertThrows<RuntimeException> { memberService.signup(signUpDto) }
        verify(exactly = 1) { memberRepository.findByEmail(signUpDto.email) }
        verify(exactly = 0) { memberRepository.save(any()) }
    }
}