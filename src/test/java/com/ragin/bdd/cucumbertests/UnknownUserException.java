package com.ragin.bdd.cucumbertests;

public class UnknownUserException extends RuntimeException {
    public UnknownUserException(final String message) {
        super(message);
    }
}
