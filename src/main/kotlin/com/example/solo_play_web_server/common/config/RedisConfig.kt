package com.example.solo_play_web_server.common.config

import com.example.solo_play_web_server.auth.dto.PendingMemberData
import com.example.solo_play_web_server.auth.entity.RefreshToken
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

@Configuration
class RedisConfig {
    @Bean
    fun redisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
        }
    }

    @Bean("verificationCodeRedisTemplate")
    fun verificationCodeRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val serializer = StringRedisSerializer.UTF_8
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, String>(serializer)
            .build()
        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean("pendingMemberDataRedisTemplate")
    fun pendingMemberDataRedisTemplate(
        factory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper
    ): ReactiveRedisTemplate<String, PendingMemberData> {
        val keySerializer = StringRedisSerializer.UTF_8
        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, PendingMemberData::class.java)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, PendingMemberData>(keySerializer)
            .value(valueSerializer)
            .build()

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