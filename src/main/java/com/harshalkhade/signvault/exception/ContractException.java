package com.harshalkhade.signvault.exception;

public class ContractException extends RuntimeException{

    public ContractException(String message) {
        super("The action cannot be execute currently: " + message);
    }
}
