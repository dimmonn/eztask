package com.local.lb.servlet.errors;


public class WrongProtocolException extends RuntimeException {
    public WrongProtocolException(String message) {
        super(message);
    }
}
