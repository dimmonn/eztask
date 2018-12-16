package com.local.lb;

import com.local.lb.balancing.Balancable;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Balancer;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoadBalancer extends LbConnectionListener implements Balancer {
    private final Balancable balancable;
    private final List<Host> hosts;
    private final Map<String, Request> requests = new ConcurrentHashMap<>();

    public LoadBalancer(List<Host> hosts, Balancable balancable) {
        this.hosts = hosts;
        this.balancable = balancable;
        if (hosts != null && !hosts.isEmpty()) {
            for (Host host : hosts) {
                host.attach(this);
            }

        }

    }

    public Host handleRequest(Request request) {

        return balancable.balance(hosts, request);
    }

    public Request getRequestById(String id) {
        return requests.get(id);
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        connectionPool.attach(this);
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }


    @Override
    public void onConnectionEstablished(String url, String content, Transport transport, UUID uuid) {
        Request request = Request.getRequestBuilder().
                setAddress(url).
                setPayload(content).
                setTransport(transport).
                setRequestId(UUID.randomUUID()).
                setConnectionId(uuid).
                build();
        requests.put(uuid.toString(), request);

    }

    @Override
    public void onRequestCompleted(Request request) {
        requests.values().remove(request);
        request.destroy();
    }
}