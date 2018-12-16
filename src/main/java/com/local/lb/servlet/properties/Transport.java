package com.local.lb.servlet.properties;

public enum Transport {
    TCP("v1.3"),
    UDP("v1.0"),
    LBPROPRIETARY("v1.0");

    private final String version;

    Transport(String version) {
        this.version= version;
    }

    public String getVersion() {
        return version;
    }
}