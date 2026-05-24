package com.harshalkhade.signvault.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super( "Resource not found in database: " + message);
    }
}
