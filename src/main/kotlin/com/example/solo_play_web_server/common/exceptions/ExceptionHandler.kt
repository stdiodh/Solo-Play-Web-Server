package com.example.solo_play_web_server.common.exceptions

import com.example.solo_play_web_server.common.dto.BaseResponse
import com.example.solo_play_web_server.common.enums.ResultStatus
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.Exception
@Order(value = 1)
@RestControllerAdvice
class ExceptionHandler {
    // Validation 예외 처리 핸들러
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun methodArgumentNotValidExceptionHandler(exception: MethodArgumentNotValidException)
            : ResponseEntity<BaseResponse<Map<String, String>>> {
        val errors = mutableMapOf<String, String>()
        exception.bindingResult.fieldErrors.forEach { error ->
            val fieldName = error.field
            val errorMsg = error.defaultMessage
            errors[fieldName] = errorMsg ?: "에러메시지가 존재하지 않습니다."
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponse(
                    status = ResultStatus.ERROR.name,
                    data = errors,
                    resultMsg = ResultStatus.ERROR.msg,
                )
            )
    }

    // 기본 예외 처리 핸들러
    @ExceptionHandler(Exception::class)
    protected fun defaultExceptionHandler(exception: Exception)
            : ResponseEntity<BaseResponse<Map<String, String>>> {
        val error = mapOf("error" to (exception.message ?: "알 수 없는 에러가 발생했습니다!"))
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            BaseResponse(
                status = ResultStatus.ERROR.name,
                data = error,
                resultMsg = ResultStatus.ERROR.msg
            )
        )
    }
}