package com.example.solo_play_web_server.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.http.HttpHeaders

@Configuration
class SwaggerConfig {

    @Bean
    fun openApi(): OpenAPI {
        val info = swaggerInfo()
        val securitySchemeName = "bearerAuth"
        val securityRequirement = SecurityRequirement().addList(securitySchemeName)
        val components = Components()
            .addSecuritySchemes(securitySchemeName, SecurityScheme()
                .name(HttpHeaders.AUTHORIZATION)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
            )

        return OpenAPI()
            .info(info)
            .addServersItem(Server().url("/"))
            .addSecurityItem(securityRequirement)
            .components(components)
    }

    private fun swaggerInfo(): Info = Info()
        .title("SoloPlay 서버 Api 명세")
        .description("A&I 2팀 프로젝트 SoloPlay 서버의 Api 명세서입니다.")
        .version("1.0.0")
}