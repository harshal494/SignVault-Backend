package com.harshalkhade.signvault.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super("Unauthorized Action: " + message);
    }
}
