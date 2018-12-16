package com.local.lb.servlet.errors;

public class LbConnectionException extends RuntimeException {
    public LbConnectionException(String message) {
        super(message);
    }
}
