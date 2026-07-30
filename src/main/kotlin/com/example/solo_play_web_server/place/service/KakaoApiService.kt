package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.dto.KakaoApiResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Service
class KakaoApiService(
    private val kakaoWebClient: WebClient,
    @Value("\${kakao.api.key}") private val kakaoApiKey: String
) {
    fun searchPlacesByKeyword(keyword: String, page: Int = 1): Mono<KakaoApiResponse> {
        logger.info { "Kakao API 호출: keyword='$keyword', page=$page" }

        return kakaoWebClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/v2/local/search/keyword.json")
                    .queryParam("query", keyword)
                    .queryParam("page", page)
                    .queryParam("size", 15)
                    .build()
            }
            .header("Authorization", "KakaoAK $kakaoApiKey")
            .retrieve()
            // 6. API 에러 처리 로직
            .onStatus(HttpStatus.BAD_REQUEST::equals) {
                logger.error { "Kakao API 요청 오류 400: ${it.statusCode()}" }
                it.bodyToMono<String>().flatMap { errorBody ->
                    Mono.error(IllegalArgumentException("잘못된 요청입니다: $errorBody"))
                }
            }
            .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals) {
                logger.error { "Kakao API 서버 오류 5xx: ${it.statusCode()}" }
                Mono.error(RuntimeException("카카오 API 서버에 문제가 발생했습니다."))
            }
            .bodyToMono<KakaoApiResponse>()
            .doOnError { error -> logger.error(error) { "Kakao API 호출 중 예외 발생" } }
    }
}
