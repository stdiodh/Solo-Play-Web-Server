package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.dto.KakaoApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class KakaoApiService(
    private val webClient: WebClient
) {
    @Value("\${kakao.api.key:DUMMY_KEY}")
    private lateinit var kakaoApiKey: String

    fun searchPlacesByKeyword(keyword: String): Mono<KakaoApiResponse> {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/v2/local/search/keyword.json")
                    .queryParam("query", keyword)
                    .build()
            }
            .header("Authorization", "KakaoAK $kakaoApiKey")
            .retrieve()
            .bodyToMono(KakaoApiResponse::class.java)
    }
}