package com.example.solo_play_web_server.common.exception

open class BusinessException(override val message: String) : RuntimeException(message)

// 실제 예외 클래스들
class EmailDuplicateException(message: String) : BusinessException(message)
class LoginFailedException(message: String) : BusinessException(message)
class VerificationCodeException(message: String) : BusinessException(message)
class SignUpProofException(message: String) : BusinessException(message)
