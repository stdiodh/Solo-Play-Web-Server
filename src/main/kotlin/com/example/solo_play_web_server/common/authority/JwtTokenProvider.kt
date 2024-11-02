package com.example.solo_play_web_server.common.authority

import com.example.solo_play_web_server.common.dto.TokenInfo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.util.*

const val EXPIRATION_MILLISECONDS = 1000 * 60 * 30L

@Component
class JwtTokenProvider {
    @Value("\$(jwt.secret)")
    lateinit var secretKey: String
    private val key by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)) }

    //토큰 생성
    fun createAccessToken(authentication: Authentication): TokenInfo {
        val authorities: String = authentication
            .authorities
            .joinToString(",", transform = GrantedAuthority::getAuthority)
        val now = Date()
        //토큰 유효시간
        val accessExpiration = Date(now.time + EXPIRATION_MILLISECONDS)
        val accessToken = Jwts
            .builder()
            .subject(authentication.name)
            .claim("auth", authorities)
            .issuedAt(now)
            .expiration(accessExpiration)
            .signWith(key, Jwts.SIG.HS256)
            .compact()
        return TokenInfo(grantType = "Bearer", accessToken = accessToken)
    }
}