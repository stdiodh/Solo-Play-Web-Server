package com.example.solo_play_web_server.auth.enum

enum class AuthProvider(val desc: String) {
    KAKAO("카카오"),
    APPLE("애플"),
    GOOGLE("구글"),
    LOCAL("자체 로그인")
}