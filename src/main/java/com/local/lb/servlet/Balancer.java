package com.local.lb.servlet;

import com.local.lb.model.Host;
import com.local.lb.servlet.errors.WrongProtocolException;
import com.local.lb.servlet.properties.Transport;

public interface Balancer {

    default void service(Request request) throws WrongProtocolException {

        if (!request.getTransport().equals(Transport.TCP) &&
                !request.getTransport().equals(Transport.UDP) &&
                !request.getTransport().equals(Transport.LBPROPRIETARY)) {
            throw new WrongProtocolException("the protocol passed is not supported");
        }
        handleRequest(request);
    }

    Host handleRequest(Request request);

    void init();

    void destroy();
}
