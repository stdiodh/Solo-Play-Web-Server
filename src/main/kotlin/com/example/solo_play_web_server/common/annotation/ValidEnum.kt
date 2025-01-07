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

    override fun initialize(annotaion: ValidEnum) {
        enumValues = annotaion.enumClass.java.enumConstants
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        // null 또는 blank는 검증하지 않음
        if (value == null) {
            return true
        }
        // enum에 포함되지 않는 값은 false 반환
        return enumValues.any { it.name == value.toString() }
    }
}