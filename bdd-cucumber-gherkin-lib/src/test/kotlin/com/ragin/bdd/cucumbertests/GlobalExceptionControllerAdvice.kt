package com.ragin.bdd.cucumbertests

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionControllerAdvice {
    @ExceptionHandler(value = [UnknownUserException::class])
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun resourceNotFoundException(ex: UnknownUserException): ErrorMessage {
        return ErrorMessage(
            code = "UNKNOWN",
            message = ex.message,
        )
    }

    data class ErrorMessage(
        var code: String? = null,
        var message: String? = null
    )
}
