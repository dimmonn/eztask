package com.local.lb.model;

import com.local.lb.LbConnectionListener;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.errors.LbConnectionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Host {
    private final List<LbConnectionListener> observers = new ArrayList<>();
    private final AtomicLong lastSubmitted = new AtomicLong(0);
    private final String name;
    private final Logger LOGGER = LogManager.getLogger(this);
    private final CopyOnWriteArrayList<Request> requests = new CopyOnWriteArrayList<>();


    public void handleRequest(Request request) throws InterruptedException {
        lastSubmitted.set(System.currentTimeMillis());
        requests.add(request);
        if (request != null) {
            Thread.sleep(new Random().nextInt(2000));
            LOGGER.info(this + " processes the request for connection " + request.getConnectionId());
            notifyLb(request);

        } else {
            throw new LbConnectionException("the connection towards LB container has not been established properly");
        }
    }

    public long getLastSubmitted() {
        return lastSubmitted.get();
    }

    private void notifyLb(Request request) {
        for (LbConnectionListener observer : observers) {
            observer.onRequestCompleted(request);
            requests.remove(request);
        }
    }

    public void attach(LbConnectionListener lbConnectionListener) {
        observers.add(lbConnectionListener);
    }

    public Host(String name) {
        this.name = name;
    }


    public synchronized int getLoad() {
        return requests.size();
    }

    @Override
    public String toString() {
        return "Host{" +
                "lastSubmitted=" + lastSubmitted +
                ", name='" + name + '\'' +
                '}';
    }

    public Host clone(Host host){
        Host tmpHost = new Host(host.name);
        tmpHost.lastSubmitted.set(host.lastSubmitted.get());
        return tmpHost;
    }

}
