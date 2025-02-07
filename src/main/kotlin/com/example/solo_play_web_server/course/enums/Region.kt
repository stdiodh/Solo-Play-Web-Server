package com.example.solo_play_web_server.course.enums

/**
 * 서울권 지역을 분리하기 위한 Enum Class
 */
enum class Region(val desc: String) {
    downtown("도심권"),
    gangbuk("강북권"),
    east_seoul("동서울권"),
    south_west("서남권"),
    south_seoul("남서울권"),
    gangnam("강남권"),
    south_east("동남권");
}