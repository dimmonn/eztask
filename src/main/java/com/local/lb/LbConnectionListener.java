package com.local.lb;

import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;

import java.util.UUID;

public abstract class LbConnectionListener {


    public abstract void onConnectionEstablished(String url, String content, Transport transport, UUID uuid);

    public abstract void onRequestCompleted(Request request);
}
