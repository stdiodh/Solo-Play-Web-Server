package com.example.solo_play_web_server.common.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig (
    @Value("\${gemini.api.url}") private val geminiApiUrl: String,
    @Value("\${gemini.api.key}") private val geminiApiKey: String
){

    @Bean
    fun kakaoWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Content-Type", "application/json;charset=UTF-8")
            .build()
    }

    @Bean("geminiWebClient")
    fun geminiWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(geminiApiUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("x-goog-api-key", geminiApiKey)
            .build()
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        // 애플리케이션 시작 시 주입된 API 키 값을 로그로 출력
        logger.info("Injected Gemini API Key starts with: {}", geminiApiKey.take(5))
    }
}