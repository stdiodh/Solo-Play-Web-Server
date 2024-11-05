package com.example.solo_play_web_server.member.controller
/**
import com.example.solo_play_web_server.member.dto.SignUpDto
import com.example.solo_play_web_server.member.service.MemberService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import com.ninjasquad.springmockk.MockkBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MemberController::class)
@AutoConfigureMockMvc
class MemberControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberService: MemberService

    val signUpDto : SignUpDto = SignUpDto("test@test.com", "test1234!@#"
        ,"test", "MAN", "2024-11-04", "test")

    @Test
    fun `회원가입 api가 성공하면 201의 응답코드와 BaseResponse안에 회원가입이 성공했습니다!의 문자열이 반환한다`() {
        every { memberService.signup(signUpDto) } returns "회원가입이 완료되었습니다!"

        mockMvc.perform(MockMvcRequestBuilders.post("/api/member/signup")
            .content("{\"email\":\"test@test.com\", \"password\":\"test1234!@#\", \"nickName\":\"test\", \"gender\":\"MAN\", \"birthday\":\"2024-11-04\", \"imageUrl\":\"test\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated)
    }
}
 **/