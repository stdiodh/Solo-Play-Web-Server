package com.example.solo_play_web_server.common.annotation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ValidEnumValidator::class])
annotation class ValidEnum(
    val message: String = "Invalid Enum Value",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val enumClass : KClass<out Enum<*>>
)

class ValidEnumValidator : ConstraintValidator<ValidEnum, Any?> {
    private lateinit var enumValues : Array<out Enum<*>>

    override fun initialize(annotation: ValidEnum) {
        enumValues = annotation.enumClass.java.enumConstants
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        // null 값을 허용하려면 true를 반환
        if (value == null) {
            return true
        }
        // 대소문자 구분 없이 Enum 값에 존재하는지 확인
        return enumValues.any { it.name.equals(value.toString(), ignoreCase = true) }
    }
}
