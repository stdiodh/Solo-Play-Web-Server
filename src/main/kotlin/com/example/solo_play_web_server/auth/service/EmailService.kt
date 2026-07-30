package com.example.solo_play_web_server.auth.service

import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

@Service
class EmailService (
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine
){
    suspend fun sendVerificationCode(toEmail: String, code: String) {
        withContext(Dispatchers.IO) {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            val context = Context()
            context.setVariable("verificationCode", code)

            val htmlContent: String = templateEngine.process("verificationCodeEmail", context)

            helper.setTo(toEmail)
            helper.setSubject("SoloPlay 회원가입 인증 코드 안내")
            helper.setText(htmlContent, true)

            mailSender.send(message)
        }
    }
}
