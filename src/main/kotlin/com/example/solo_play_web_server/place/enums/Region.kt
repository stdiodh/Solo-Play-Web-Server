package com.example.solo_play_web_server.place.enums

/**
 * 서울권 지역을 분리하기 위한 Enum Class
 */
enum class Region(
    val zone: Zone,
    val districtName: String
) {
    // 강남권
    GANGNAM(Zone.GANGNAM, "강남구"),
    SEOCHO(Zone.GANGNAM, "서초구"),

    // 도심권
    EUNPYEONG(Zone.CENTRAL, "은평구"),
    JONGNO(Zone.CENTRAL, "종로구"),
    SEODAEMUN(Zone.CENTRAL, "서대문구"),
    JUNG(Zone.CENTRAL, "중구"),
    MAPO(Zone.CENTRAL, "마포구"),
    YONGSAN(Zone.CENTRAL, "용산구"),

    // 강북권
    DOBONG(Zone.GANGBOK, "도봉구"),
    NOWON(Zone.GANGBOK, "노원구"),
    SEONGBUK(Zone.GANGBOK, "성북구"),
    GANGBUK(Zone.GANGBOK, "강북구"),

    // 동서울권
    DONGDAEMUN(Zone.EASTSEOUL, "동대문구"),
    JUNGNANG(Zone.EASTSEOUL, "중랑구"),
    SEONGDONG(Zone.EASTSEOUL, "성동구"),
    GWANGJIN(Zone.EASTSEOUL, "광진구"),

    // 서남권
    GANGSEO(Zone.SOUTHWEST, "강서구"),
    YANGCHEON(Zone.SOUTHWEST, "양천구"),

    // 남서울권
    YEONGDEUNGPO(Zone.SOUTH, "영등포구"),
    DONGJAK(Zone.SOUTH, "동작구"),
    GURO(Zone.SOUTH, "구로구"),
    GWANAK(Zone.SOUTH, "관악구"),
    GEUMCHEON(Zone.SOUTH, "금천구"),

    // 동남권
    SONGPA(Zone.SOUTHEAST, "송파구"),
    GANGDONG(Zone.SOUTHEAST, "강동구");

    enum class Zone(val displayName: String) {
        CENTRAL("도심권"),
        GANGBOK("강북권"),
        EASTSEOUL("동서울권"),
        SOUTHWEST("서남권"),
        SOUTH("남서울권"),
        GANGNAM("강남권"),
        SOUTHEAST("동남권")
    }
}
