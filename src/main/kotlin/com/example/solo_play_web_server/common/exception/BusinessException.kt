package com.example.solo_play_web_server.common.exception

open class BusinessException(override val message: String) : RuntimeException(message)

// 실제 예외 클래스들
class EmailDuplicateException(message: String = "이미 사용 중인 이메일입니다.") : BusinessException(message)
class NicknameDuplicateException(message: String = "이미 사용 중인 닉네임입니다.") : BusinessException(message)
class LoginFailedException(message: String = "가입되지 않은 이메일이거나, 비밀번호가 올바르지 않습니다.") : BusinessException(message)
class InvalidTokenException(message: String) : BusinessException(message)