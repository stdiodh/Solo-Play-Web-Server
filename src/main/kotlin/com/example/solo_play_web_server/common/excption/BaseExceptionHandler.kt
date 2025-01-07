package com.example.solo_play_web_server.common.excption

import com.example.solo_play_web_server.common.dtos.ErrorResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException

@ControllerAdvice
class BaseExceptionHandler {
    @Order(-2)
    @ExceptionHandler(WebExchangeBindException::class)
    protected fun webExchangeBindExceptionHandler(
        exception: WebExchangeBindException
    ) : ResponseEntity<ErrorResponse<Map<String, String>>> {
        val errors = mutableMapOf<String, String>()
        println("${exception.bindingResult.allErrors}")
        exception.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMsg = error.defaultMessage
            errors[fieldName] = errorMsg ?: "에러 메시지가 존재하지 않습니다!"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                data = errors,
            )
        )
    }
    @Order(-2)
    @ExceptionHandler(Exception::class)
    protected fun exceptionHandler(ex: Exception)
            : ResponseEntity<ErrorResponse<String>>
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse<String>(data = ex.message ?: "에러가 발생했습니다.")
        )
    }
}