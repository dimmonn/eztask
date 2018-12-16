package com.local.lb.balancing.algorythm;

import com.local.lb.LoadBalancer;
import com.local.lb.connection.Connection;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.errors.WrongLbConfig;
import com.local.lb.servlet.properties.Transport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoundRobiinTest {

    private static ConnectionPool connectionPool;
    private static ExecutorService executorService;
    private static final int POOL_SIZE = 10;
    private static final int nThreads = 2;
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
        executorService = Executors.newFixedThreadPool(nThreads);
    }

    @Test
    public void balanceSequentialTest() {
        List<Host> order = new ArrayList<>();
        LoadBalancer balancer = new LoadBalancer(hosts, new RoundRobiin());
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
            Assert.assertTrue(order.get(j).getLastSubmitted() <= order.get(j + 1).getLastSubmitted());
        }
    }

    @Test
    public void balanceThreadedTest() {
        LoadBalancer balancer = new LoadBalancer(hosts, new RoundRobiin());
        balancer.setConnectionPool(connectionPool);
        CompletableFuture.allOf(generateTask(balancer).thenAccept(e -> {
                    for (int i = 0; i < e.size() - 1; i++) {
                        Assert.assertTrue(e.get(i).getLastSubmitted() <= e.get(i + 1).getLastSubmitted());
                    }
                }),
                generateTask(balancer).thenAccept(e -> {
                    for (int i = 0; i < e.size() - 1; i++) {
                        Assert.assertTrue(e.get(i).getLastSubmitted() <= e.get(i + 1).getLastSubmitted());
                    }
                }),
                generateTask(balancer).thenAccept(e -> {
                    for (int i = 0; i < e.size() - 1; i++) {
                        Assert.assertTrue(e.get(i).getLastSubmitted() <= e.get(i + 1).getLastSubmitted());
                    }
                })).join();
    }

    private CompletableFuture<List<Host>> generateTask(LoadBalancer balancer) {
        return CompletableFuture.supplyAsync(() -> {
            List<Host> order = new ArrayList<>();
            for (int i = 0; i < BALANCING_NUM * 2; i++) {
                try (Connection connection = connectionPool.
                        getConnection("http://example.com", "testContent", Transport.TCP)) {
                    Request request = balancer.getRequestById(connection.getUuid().toString());
                    Collections.synchronizedList(order).add(balancer.handleRequest(request));
                } catch (Exception e) {
                    LOGGER.info(e.getMessage());
                }
            }
            return order;
        }, executorService);
    }

    @Test(expected = WrongLbConfig.class)
    public void wrongLbConfigTest() throws Exception {
        LoadBalancer balancer = new LoadBalancer(null, new RoundRobiin());
        try (Connection connection = connectionPool.
                getConnection("http://example.com", "testContent", Transport.TCP)) {
            Request request = balancer.getRequestById(connection.getUuid().toString());
            balancer.handleRequest(request);
        }

    }
}