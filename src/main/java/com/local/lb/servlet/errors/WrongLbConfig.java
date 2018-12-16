package com.local.lb.servlet.errors;

public class WrongLbConfig extends RuntimeException {
    public WrongLbConfig(String message) {
        super(message);
    }
}
