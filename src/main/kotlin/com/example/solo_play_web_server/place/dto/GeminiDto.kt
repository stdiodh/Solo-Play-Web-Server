package com.example.solo_play_web_server.place.dto

data class GeminiApiResponse(val candidates: List<Candidate>)
data class Candidate(val content: Content)
data class Content(val parts: List<Part>)
data class Part(val text: String)

data class EnrichedPlaceData(
    val displayTitle: String,
    val displayTags: List<String>
)