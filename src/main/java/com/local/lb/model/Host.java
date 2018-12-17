package com.local.lb.model;

import com.local.lb.LbConnectionListener;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.errors.LbConnectionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Host implements HostMBean {
    private final List<LbConnectionListener> observers = new ArrayList<>();
    private final AtomicLong lastSubmitted = new AtomicLong(0);
    private final String name;
    private final Logger LOGGER = LogManager.getLogger(this);
    private final CopyOnWriteArrayList<Request> requests = new CopyOnWriteArrayList<>();
    private boolean isDamaged;

    public String getName() {
        return name;
    }

    public void handleRequest(Request request) throws InterruptedException {
        if (!isDamaged) {
            requests.add(request);
            if (request != null) {
                Thread.sleep(new Random().nextInt(2000));
                LOGGER.info(this + " processes the request for connection " + request.getConnectionId());
                notifyLb(request);
                lastSubmitted.set(System.currentTimeMillis());
            } else {
                throw new LbConnectionException("the connection towards LB container has not been established properly");
            }
        } else {
            lastSubmitted.set(Long.MAX_VALUE);
            notifyLb(request);
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


    public int getNumberOfActiveRequests() {
        return requests.size();
    }


    @Override
    public String toString() {
        return "Host{" +
                "lastSubmitted=" + lastSubmitted +
                ", name='" + name + '\'' + ". load='" + requests.size() +
                '}';
    }


    public Host clone(Host host) {
        Host tmpHost = new Host(host.name);
        tmpHost.lastSubmitted.set(host.lastSubmitted.get());
        return tmpHost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return name.equals(host.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean isDamaged() {

        return isDamaged;
    }

    @Override
    public void setDamaged(boolean damaged) {
        if (!damaged) {
            LOGGER.warn("the hostname "+name+" is back to service");
            lastSubmitted.set(0);
            requests.clear();
        }else {
            LOGGER.warn("The host " + name + " is damaged and is not balancing a traffic");
        }
        this.isDamaged = damaged;
    }
}
