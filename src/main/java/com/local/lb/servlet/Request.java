package com.local.lb.servlet;

import com.local.lb.servlet.properties.Address;
import com.local.lb.servlet.properties.Payload;
import com.local.lb.servlet.properties.Transport;

import java.util.UUID;

public class Request {
    private Payload payload;
    private Address address;
    private Transport transport;
    private UUID requestId;
    private UUID connectionId;

    private Request() {

    }

    public Transport getTransport() {
        return transport;
    }

    public static RequestBuilder getRequestBuilder() {
        return new Request().new RequestBuilder();
    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public void destroy() {
        connectionId=null;
        transport=null;
        requestId=null;
    }

    public class RequestBuilder {


        public RequestBuilder setConnectionId(UUID connectionId) {

            Request.this.connectionId = connectionId;
            return this;
        }

        public RequestBuilder setPayload(String payload) {
            Request.this.payload = new Payload(payload);
            return this;

        }

        public RequestBuilder setAddress(String url) {
            Request.this.address = new Address(url);
            return this;
        }

        public RequestBuilder setRequestId(UUID id) {
            Request.this.requestId = id;
            return this;
        }


        public RequestBuilder setTransport(Transport transport) {
            Request.this.transport = transport;
            return this;
        }

        //make it immutable
        public Request build() {
            Request request = new Request();
            request.transport = Request.this.transport;
            request.address = Request.this.address;
            request.payload = Request.this.payload;
            request.requestId =Request.this.requestId;
            request.connectionId =Request.this.connectionId;
            return request;
        }
    }

}
