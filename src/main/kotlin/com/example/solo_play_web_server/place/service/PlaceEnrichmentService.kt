package com.example.solo_play_web_server.place.service

import com.example.solo_play_web_server.place.dto.EnrichedPlaceData
import com.example.solo_play_web_server.place.dto.GeminiApiResponse
import com.example.solo_play_web_server.place.entity.Place
import com.example.solo_play_web_server.place.repository.PlaceRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PlaceEnrichmentService internal constructor(
    private val placeRepository: PlaceRepository,
    private val webClient: WebClient,
    private val enrichmentScope: CoroutineScope
) {
    @Autowired
    constructor(
        placeRepository: PlaceRepository,
        @Qualifier("geminiWebClient") webClient: WebClient
    ) : this(
        placeRepository,
        webClient,
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    )

    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    fun enrichPlaceData(place: Place): Job {
        return enrichmentScope.launch {
            try {
                val requestBody = createGeminiRequestBody(place)
                val response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiApiResponse::class.java)
                    .awaitSingle()

                val jsonText = response.candidates.first().content.parts.first().text
                    .replace("```json", "").replace("```", "").trim()

                val enrichedData = objectMapper.readValue(jsonText, EnrichedPlaceData::class.java)

                place.displayTitle = enrichedData.displayTitle
                place.displayTags = enrichedData.displayTags
                placeRepository.save(place).awaitSingle()
                logger.info("Successfully enriched place data for: ${place.placeName}")

            } catch (e: Exception) {
                logger.error("Failed to enrich place data for ${place.placeName}: ${e.message}")
            }
        }
    }

    private fun createGeminiRequestBody(place: Place): Map<String, Any> {
        val prompt = """
            너는 '혼자 놀기' 앱 'SoloPlay'의 콘텐츠 어시스턴트다.
            아래 장소 정보로 '장소 설명'과 '특징 태그'를 JSON으로 생성해줘.

            [입력 정보]
            - 장소 이름: "${place.placeName}"
            - 카테고리: "${place.kakaoCategoryName}"
            - 주소: "${place.address}"

            [생성 규칙]
            1. '장소 설명' (displayTitle): 15자 내외의 매력적인 한글 문장 1개.
            2. '특징 태그' (displayTags): 3~5개의 핵심 특징을 추출하여 '#'으로 시작하는 한글 태그.

            [출력 형식]
            반드시 아래 JSON 형식으로만 응답해줘. 다른 설명은 절대 추가하지 마.
            {
              "displayTitle": "여기에 장소 설명 생성",
              "displayTags": ["#태그1", "#태그2", "#태그3"]
            }
        """.trimIndent()
        return mapOf("contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))))
    }
}
