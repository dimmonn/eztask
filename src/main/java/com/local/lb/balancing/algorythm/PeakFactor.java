package com.local.lb.balancing.algorythm;

import com.local.lb.balancing.Balancable;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.errors.LbConnectionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PeakFactor implements Balancable {
    private final Logger LOGGER = LogManager.getLogger(this);

    public Host balance(List<Host> hosts, Request request) {
        LOGGER.info(new Date() + "================= PeakFactor is started =================");
        long hostMaxLoad = hosts.
                stream().
                max(Comparator.comparing(Host::getNumberOfActiveRequests)).
                orElseThrow(() -> new LbConnectionException("connection is not established")).
                getNumberOfActiveRequests();
        Host hostMinLoad = hosts.
                stream().
                min(Comparator.comparing(Host::getNumberOfActiveRequests)).
                orElseThrow(() -> new LbConnectionException("connection is not established"));
        Host hostPicked = null;
        try {
            hostPicked = hosts.stream().
                    filter(e -> (float)e.getNumberOfActiveRequests() / (hostMaxLoad == 0 ? 1 : hostMaxLoad) < 0.75).
                    findAny().
                    orElse(hostMinLoad);

            hostPicked.handleRequest(request);

            LOGGER.info(new Date() + "================= PeakFactor is stopeed =================" + hostPicked.getNumberOfActiveRequests());
            return hostPicked;
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        return hostPicked;
    }

}
