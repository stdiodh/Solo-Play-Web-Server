package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.dto.KakaoApiResponse
import com.example.solo_play_web_server.place.dto.Meta
import com.example.solo_play_web_server.place.entity.KakaoPlace
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.util.function.Function
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class KakaoApiServiceSpec : BehaviorSpec() {
    private val webClient = mockk<WebClient>()
    private val kakaoApiService = KakaoApiService(webClient)

    init {
        Given("장소 검색을 위한 키워드가 주어졌을 때") {
            val keyword = "카페"
            val expectedApiResponse = KakaoApiResponse(
                documents = listOf(
                    KakaoPlace("1", "스타벅스", "음식점 > 카페", "서울", "서울", "http://place.map.kakao.com/1")
                ),
                meta = Meta(true, 1)
            )

            val apiKeyField = KakaoApiService::class.memberProperties
                .find { it.name == "kakaoApiKey" }
            apiKeyField?.let {
                it.isAccessible = true
                (it as kotlin.reflect.KMutableProperty<*>).setter.call(kakaoApiService, "TEST_API_KEY")
            }

            val uriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
            val headersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
            val responseSpec = mockk<WebClient.ResponseSpec>()

            every { webClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, URI>>()) } returns headersSpec
            every { headersSpec.header(any(), any()) } returns headersSpec
            every { headersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(KakaoApiResponse::class.java) } returns Mono.just(expectedApiResponse)

            When("카카오 API 서비스로 장소를 검색하면") {
                val actualResponse = kakaoApiService.searchPlacesByKeyword(keyword).block()

                Then("미리 정의한 가짜 데이터를 반환해야 한다") {
                    actualResponse shouldNotBe null
                    actualResponse?.documents?.size shouldBe 1
                    actualResponse?.documents?.get(0)?.placeName shouldBe "스타벅스"
                }
            }
        }
    }
}