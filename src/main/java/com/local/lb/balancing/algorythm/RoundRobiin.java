package com.local.lb.balancing.algorythm;

import com.local.lb.balancing.Balancable;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.errors.LbConnectionException;
import com.local.lb.servlet.errors.WrongLbConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RoundRobiin implements Balancable {
    private final Logger LOGGER = LogManager.getLogger(this);

    public Host balance(List<Host> hosts, Request request) {
        LOGGER.info(new Date() + "================= RoundRobiin is running =================");
        if (hosts == null || hosts.isEmpty()) {
            throw new WrongLbConfig("hosts are not added into the load balancer");
        }
        try {
            Host host = hosts.stream().
                    min(Comparator.comparing(Host::getLastSubmitted)).
                    orElseThrow(() -> new LbConnectionException("connection is not established"));
            Host tmpHost = host.clone(host);
            host.handleRequest(request);
            LOGGER.info(new Date() + "================= RoundRobiin is stopeed =================" + host.getLastSubmitted() + " " + host.getLoad());
            return tmpHost;
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }
}
