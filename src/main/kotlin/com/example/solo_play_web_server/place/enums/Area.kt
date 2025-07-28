package com.example.solo_play_web_server.place.enums

/**
 * 서울권 지역을 분리하기 위한 Enum Class
 */
enum class Area(
    val desc : String
) {
    // 강남권
    GANGNAM("강남구"),
    SEOCHO("서초구"),

    // 도심권
    EUNPYEONG("은평구"),
    JONGNO("종로구"),
    SEODAEMUN("서대문구"),
    JUNG("중구"),
    MAPO("마포구"),
    YONGSAN("용산구"),

    // 강북권
    DOBONG("도봉구"),
    NOWON("노원구"),
    SEONGBUK("성북구"),
    GANGBUK("강북구"),

    // 동서울권
    DONGDAEMUN("동대문구"),
    JUNGNANG("중랑구"),
    SEONGDONG("성동구"),
    GWANGJIN("광진구"),

    // 서남권
    GANGSEO("강서구"),
    YANGCHEON("양천구"),

    // 남서울권
    YEONGDEUNGPO("영등포구"),
    DONGJAK("동작구"),
    GURO("구로구"),
    GWANAK("관악구"),
    GEUMCHEON("금천구"),

    // 동남권
    SONGPA("송파구"),
    GANGDONG("강동구")
}
