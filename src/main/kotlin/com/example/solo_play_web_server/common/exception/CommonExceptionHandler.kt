package com.example.solo_play_web_server.common.exception

import com.example.solo_play_web_server.common.dtos.BaseResponse
import com.example.solo_play_web_server.common.enum.ResultStatus
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@Order(value = 2)
@RestControllerAdvice
class CommonExceptionHandler {

    // @Valid 관련 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun methodArgumentNotValidExceptionHandler(
        exception: MethodArgumentNotValidException
    ): ResponseEntity<BaseResponse<Map<String, String>>> {
        val errors = mutableMapOf<String, String>()

        exception.bindingResult.allErrors.forEach { error ->
            // 필드 이름과 에러 메시지를 매핑
            if (error is FieldError) {
                val fieldName = error.field
                val errorMsg = error.defaultMessage ?: "에러 메시지가 존재하지 않습니다!"
                errors[fieldName] = errorMsg
            } else {
                // ObjectError의 경우 처리 (추후 필요 시 확장 가능)
                errors["global"] = error.defaultMessage ?: "에러 메시지가 존재하지 않습니다!"
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            BaseResponse(
                status = ResultStatus.ERROR.name,
                data = errors,
                resultMsg = "입력값 검증 오류 발생"
            )
        )
    }

    // NoHandlerFoundException 처리
    @ExceptionHandler(WebExchangeBindException::class)
    protected fun notFoundApiUrlExceptionHandler(exception: WebExchangeBindException):
            ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            BaseResponse(status = ResultStatus.ERROR.name, resultMsg = "존재하지 않는 API URL 입니다.")
        )
    }

    // 기본 예외 처리 (그 외 예외에 대한 기본 처리)
    @ExceptionHandler(Exception::class)
    protected fun defaultExceptionHandler(exception: Exception):
            ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            BaseResponse(status = ResultStatus.ERROR.name, resultMsg = "알 수 없는 오류가 발생했습니다.")
        )
    }
}
