package com.example.solo_play_web_server.auth.service

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import io.mockk.impl.annotations.MockK
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine


class EmailServiceSpec : BehaviorSpec() {
    @MockK
    lateinit var mailSender: JavaMailSender
    @MockK
    lateinit var templateEngine: SpringTemplateEngine
    lateinit var emailService: EmailService

    init {
        beforeTest {
            MockKAnnotations.init(this)
            emailService = EmailService(mailSender, templateEngine)
        }

        afterTest {
            clearAllMocks()
        }

        Given("이메일 발송 서비스에서") {
            val toEmail = "test@example.com"
            val code = "123456"
            val mimeMessage = mockk<MimeMessage>(relaxed = true)

            When("sendVerificationCode 메소드가 호출되면") {
                every { templateEngine.process(any<String>(), any<Context>()) } returns "html"
                every { mailSender.createMimeMessage() } returns mimeMessage
                every { mailSender.send(any<MimeMessage>()) } returns Unit

                // Act
                emailService.sendVerificationCode(toEmail, code)

                Then("메일 전송 로직이 호출된다") {
                    verify(exactly = 1) { templateEngine.process(any<String>(), any<Context>()) }
                    verify(exactly = 1) { mailSender.send(any<MimeMessage>()) }
                }
            }
        }
    }
}
