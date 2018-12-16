package com.local.lb.balancing.algorythm;

import com.local.lb.LoadBalancer;
import com.local.lb.connection.Connection;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PeakFactorTest {
    private static ConnectionPool connectionPool;
    private static final int POOL_SIZE = 10;
    private static final int HOSTS_NUMBER = 4;
    private static final int BALANCING_NUM = 10;
    private static final List<Host> hosts = new ArrayList<>(HOSTS_NUMBER);
    private final Logger LOGGER = LogManager.getLogger(this);


    @BeforeClass
    public static void setUp() {
        for (int i = 1; i <= HOSTS_NUMBER; i++) {
            hosts.add(new Host("TEST_" + i));
        }
        connectionPool = new ConnectionPool(POOL_SIZE);
    }

    @Test
    public void balanceSequentialTest() {
        List<Host> order = new ArrayList<>();
        LoadBalancer balancer = new LoadBalancer(hosts, new PeakFactor());
        balancer.setConnectionPool(connectionPool);
        for (int i = 0; i < BALANCING_NUM * 2; i++) {
            try (Connection connection = connectionPool.
                    getConnection("http://example.com", "testContent", Transport.TCP)) {
                Request request = balancer.getRequestById(connection.getUuid().toString());
                order.add(balancer.handleRequest(request));
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
            }

        }
        for (int j = 0; j < order.size() - 1; j++) {
            System.out.println(order.get(j).getNumberOfActiveRequests() + " " + order.stream().map(Host::getNumberOfActiveRequests).collect(Collectors.toList()));
            Assert.assertTrue(order.get(j).getNumberOfActiveRequests() < 0.75 || Integer.valueOf(order.get(j).getNumberOfActiveRequests()).
                    equals(order.stream().min(Comparator.comparing(Host::getNumberOfActiveRequests))));
        }
    }

}