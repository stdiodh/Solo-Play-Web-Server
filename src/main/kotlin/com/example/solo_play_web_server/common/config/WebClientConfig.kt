package com.example.solo_play_web_server.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun kakaoWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Content-Type", "application/json;charset=UTF-8")
            .build()
    }
}