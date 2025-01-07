package com.example.solo_play_web_server.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openApi() : OpenAPI = OpenAPI()
        .components(Components())
        .info(swaggerInfo())
        .addServersItem(Server().url("/"))

    private fun swaggerInfo() : Info = Info()
        .title("SoloPlay 서버 Api 명세")
        .description("A&I 2팀 프로젝트 SoloPlay 서버의 Api 명세서입니다.")
        .version("1.0.0")
}