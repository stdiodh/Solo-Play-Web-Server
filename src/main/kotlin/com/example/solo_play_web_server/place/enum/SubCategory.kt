package com.example.solo_play_web_server.place.enums

import com.example.solo_play_web_server.place.enum.MainCategory

enum class SubCategory(
    val mainCategory: MainCategory,
    val desc: String
) {
    // Relax / Leisure
    CAFE(MainCategory.RELAX_LEISURE, "카페 (브런치 카페, 디저트 카페, 북카페 등)"),
    PARK(MainCategory.RELAX_LEISURE, "공원 / 산책로"),
    SPA(MainCategory.RELAX_LEISURE, "스파 / 찜질방 / 온천"),
    LIBRARY(MainCategory.RELAX_LEISURE, "도서관 / 독서실"),

    // Culture / Art
    CINEMA(MainCategory.CULTURE_ART, "영화관"),
    MUSEUM(MainCategory.CULTURE_ART, "미술관 / 박물관 / 전시회"),
    THEATER(MainCategory.CULTURE_ART, "공연장 (연극, 뮤지컬, 콘서트)"),
    BOOKSTORE(MainCategory.CULTURE_ART, "독립서점 / 북클럽"),

    // Activity / Experience
    SPORTS(MainCategory.ACTIVITY_EXPERIENCE, "스포츠 (헬스장, 클라이밍, 볼링, 탁구, 스크린 골프)"),
    VR_AR(MainCategory.ACTIVITY_EXPERIENCE, "VR/AR 체험"),
    ESCAPE_ROOM(MainCategory.ACTIVITY_EXPERIENCE, "방탈출 / 보드게임 카페 (솔로 가능 버전)"),
    WORKSHOP(MainCategory.ACTIVITY_EXPERIENCE, "요리/공방 클래스 (도예, 캔들, 베이킹 등)"),

    // Travel / Exploration
    NEAR_TRIP(MainCategory.TRAVEL_EXPLORATION, "근교 여행 (산, 바다, 캠핑장)"),
    CITY_TOUR(MainCategory.TRAVEL_EXPLORATION, "도심 투어 (전통시장, 랜드마크, 테마 거리)"),
    SOLO_STAY(MainCategory.TRAVEL_EXPLORATION, "혼자 가는 숙소 (게스트하우스, 1인 호텔룸)"),

    // Gourmet / Food
    SOLO_RESTAURANT(MainCategory.GOURMET_FOOD, "1인 전문 식당 (라멘, 고기, 백반집)"),
    DESSERT_CAFE(MainCategory.GOURMET_FOOD, "디저트 카페"),
    STREET_FOOD(MainCategory.GOURMET_FOOD, "길거리 음식 / 야시장"),
    FINE_DINING(MainCategory.GOURMET_FOOD, "파인다이닝 (혼자 도전용)"),

    // Healing / Self Improvement
    YOGA_MEDITATION(MainCategory.HEALING_SELF_IMPROVEMENT, "요가 / 명상 센터"),
    ONE_DAY_CLASS(MainCategory.HEALING_SELF_IMPROVEMENT, "원데이 클래스 (그림, 글쓰기, 음악)"),
    STUDY_CAFE(MainCategory.HEALING_SELF_IMPROVEMENT, "북카페 / 스터디룸"),
    COUNSELING(MainCategory.HEALING_SELF_IMPROVEMENT, "상담/코칭 프로그램"),

    // Special Experience
    THEME_PARK(MainCategory.SPECIAL_EXPERIENCE, "놀이공원 / 테마파크"),
    KARAOKE(MainCategory.SPECIAL_EXPERIENCE, "노래방 (1인 노래방 포함)"),
    OVERSEAS_TRAVEL(MainCategory.SPECIAL_EXPERIENCE, "여행/해외여행"),
    FULL_COURSE_MEAL(MainCategory.SPECIAL_EXPERIENCE, "레스토랑 풀코스")
}