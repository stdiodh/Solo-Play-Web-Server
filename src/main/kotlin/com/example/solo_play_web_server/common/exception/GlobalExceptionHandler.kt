package com.example.solo_play_web_server.common.exception

import com.example.solo_play_web_server.common.dto.ApiResponse
import com.example.solo_play_web_server.common.dto.ResultStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * DTO 유효성 검증(@Valid) 실패 시
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationExceptions(ex: WebExchangeBindException): ResponseEntity<ApiResponse<*>> {
        // 필드별 에러 메시지를 Map으로 수집
        val details = ex.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "유효하지 않은 값입니다.") }

        // 실패 시 data 필드에 details 맵을 담아 반환
        val response = ApiResponse(
            status = ResultStatus.ERROR,
            message = "입력값 유효성 검사에 실패했습니다.",
            data = details
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleInputException(ex: ServerWebInputException): ResponseEntity<ApiResponse<Void>> {
        logger.warn("Invalid request input: {}", ex.reason)

        val response = ApiResponse<Void>(
            status = ResultStatus.ERROR,
            message = "입력값이 올바르지 않습니다."
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * 직접 정의한 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Void>> {
        logger.warn("Business exception: {}", ex.message)

        val response = ApiResponse<Void>(
            status = ResultStatus.ERROR,
            message = ex.message
        )

        // 예외 종류에 따라 적절한 HTTP 상태 코드 결정
        val status = when (ex) {
            is EmailDuplicateException -> HttpStatus.CONFLICT
            is LoginFailedException -> HttpStatus.UNAUTHORIZED
            is VerificationCodeException -> HttpStatus.UNAUTHORIZED
            is SignUpProofException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.BAD_REQUEST // 400
        }
        return ResponseEntity.status(status).body(response)
    }

    /**
     * 처리하지 못한 모든 예외 (500 서버 에러)
     */
    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception): ResponseEntity<ApiResponse<Void>> {
        logger.error("알 수 없는 에러가 발생하였습니다.", ex)

        val response = ApiResponse<Void>(
            status = ResultStatus.ERROR,
            message = ResultStatus.ERROR.msg // Enum에 정의된 기본 에러 메시지 사용
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
