package com.local.lb.connection;

import com.local.lb.servlet.properties.Transport;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConnectionPoolTest {

    private ConnectionPool connectionPool;
    private static ExecutorService executorService;
    private static final int SIZE = 10;
    private static final int nThreads = 2;
    private static final int DELTA = 5;

    @BeforeClass
    public static void setUpOnce() {

        executorService = Executors.newFixedThreadPool(nThreads);
    }

    @Before
    public void setUp() {
        connectionPool = new ConnectionPool(SIZE);
    }

    @Test
    public void getConnectionTest() {

        List<Connection> connections = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            connections.add(connectionPool.getConnection("testUrl", "content", Transport.TCP));
            Assert.assertEquals(i, connectionPool.availableConnections());
        }
        int i = 0;
        for (Connection connection : connections) {
            connectionPool.releaseConnection(connection);
            Assert.assertEquals(++i, connectionPool.availableConnections());
        }
        for (Connection connection : connections) {
            connection.close();
            Assert.assertNull(connection.getUuid());
            Assert.assertNull(connection.getContent());
            Assert.assertNull(connection.getTransport());
            Assert.assertNull(connection.getUrl());

        }
    }

    @Test
    public void getThreadedConnectionTest() throws ExecutionException, InterruptedException {
        List<Connection> conns = new ArrayList<>();
        CompletableFuture<Integer> acquiredConnections = CompletableFuture.supplyAsync(() -> {
            int j = 0;
            for (int i = 0; i < SIZE + DELTA; i++) {
                conns.add(connectionPool.getConnection("testUrl", "content", Transport.TCP));
                j++;
            }
            return j;
        }, executorService).thenApply(e -> e);
        Thread.sleep(1000);
        CompletableFuture.supplyAsync(() -> {
            for (int i = 0; i < SIZE; i++) {
                if (conns.get(i) != null) {
                    connectionPool.releaseConnection(conns.get(i));
                }
            }
            return true;
        }, executorService).join();
        Assert.assertEquals(15, acquiredConnections.get().intValue());
        Assert.assertEquals(5, connectionPool.availableConnections());

    }

    @AfterClass
    public static void tearDown() {
        executorService.shutdown();
    }
}