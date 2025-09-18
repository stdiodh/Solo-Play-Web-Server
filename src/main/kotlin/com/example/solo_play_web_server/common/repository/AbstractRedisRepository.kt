package com.example.solo_play_web_server.common.repository

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import java.time.Duration

abstract class AbstractRedisRepository<V : Any> {
    abstract val redisTemplate: ReactiveRedisTemplate<String, V>
    abstract val keyPrefix: String

    protected fun getKey(keySuffix: String) = "$keyPrefix$keySuffix"

    protected suspend fun save(keySuffix: String, value: V, expiry: Duration): Boolean {
        return redisTemplate.opsForValue().set(getKey(keySuffix), value, expiry).awaitSingleOrNull() ?: false
    }

    protected suspend fun find(keySuffix: String): V? {
        return redisTemplate.opsForValue().get(getKey(keySuffix)).awaitSingleOrNull()
    }

    protected suspend fun delete(keySuffix: String): Boolean {
        return redisTemplate.opsForValue().delete(getKey(keySuffix)).awaitSingleOrNull() ?: false
    }
}