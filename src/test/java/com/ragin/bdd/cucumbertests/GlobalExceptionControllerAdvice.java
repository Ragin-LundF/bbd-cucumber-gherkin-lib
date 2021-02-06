package com.ragin.bdd.cucumbertests;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionControllerAdvice {
    @ExceptionHandler(value = {UnknownUserException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorMessage resourceNotFoundException(final UnknownUserException ex) {
        final ErrorMessage message = new ErrorMessage();
        message.setCode("UNKNOWN");
        message.setMessage(ex.getMessage());

        return message;
    }

    @Data
    public static class ErrorMessage {
        String code;
        String message;
    }
}
