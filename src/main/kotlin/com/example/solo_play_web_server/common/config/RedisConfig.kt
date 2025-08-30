package com.example.solo_play_web_server.common.config

import com.example.solo_play_web_server.auth.dto.VerificationData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import com.example.solo_play_web_server.auth.entity.RefreshToken

@Configuration
class RedisConfig {
    @Bean("verificationDataRedisTemplate")
    fun verificationDataRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, VerificationData> {
        // 1. ObjectMapper 설정: Kotlin과 Java 8의 날짜/시간 타입을 지원하도록 설정
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
        }

        // 2. Value를 위한 직렬화기(Serializer) 설정: PendingMember 객체를 JSON으로 변환
        // 생성자에 ObjectMapper와 클래스 타입을 함께 전달
        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, VerificationData::class.java)

        // 3. Key를 위한 직렬화기 설정: String을 사용
        val keySerializer = StringRedisSerializer.UTF_8

        // 4. 직렬화 컨텍스트 생성
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, VerificationData>(keySerializer)
            .value(valueSerializer)       // Value 직렬화 방식 설정
            .hashValue(valueSerializer)   // Hash의 Value 직렬화 방식 설정
            .build()

        // 5. 설정이 적용된 ReactiveRedisTemplate 생성
        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean("refreshTokenRedisTemplate")
    fun refreshTokenRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, RefreshToken> {
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
        }

        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, RefreshToken::class.java)
        val keySerializer = StringRedisSerializer.UTF_8

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, RefreshToken>(keySerializer)
            .value(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }
}