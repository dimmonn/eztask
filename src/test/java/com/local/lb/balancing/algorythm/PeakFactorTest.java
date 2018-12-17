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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        for (Host host : order) {
            Assert.assertEquals(0,host.getNumberOfActiveRequests());
        }
    }

    @Test
    public void lessThenThreeFourthTest() {
        for (int i = 1; i < 50; i++) {
            Host hostX = mock(Host.class);
            when(hostX.getNumberOfActiveRequests()).thenReturn(i);
            Host hostY = mock(Host.class);
            when(hostY.getNumberOfActiveRequests()).thenReturn(2 * i);
            Host hostZ = mock(Host.class);
            when(hostZ.getNumberOfActiveRequests()).thenReturn(3 * i);
            Host hostF = mock(Host.class);
            when(hostF.getNumberOfActiveRequests()).thenReturn(4 * i);
            List<Host> hosts = Arrays.asList(hostX, hostY, hostZ, hostF);
            LoadBalancer lb = new LoadBalancer(hosts, new PeakFactor());
            Host hostPicked = lb.handleRequest(null);
            Assert.assertTrue((float)hostPicked.getNumberOfActiveRequests() / (4 * i) < 0.75);
        }

    }

    @Test
    public void moreThenTreeFourth() {
        for (int i = 1; i < 50; i++) {
            Host hostX = mock(Host.class);
            when(hostX.getNumberOfActiveRequests()).thenReturn(50+i);
            Host hostY = mock(Host.class);
            when(hostY.getNumberOfActiveRequests()).thenReturn(51+i);
            Host hostZ = mock(Host.class);
            when(hostZ.getNumberOfActiveRequests()).thenReturn(52+i);
            Host hostF = mock(Host.class);
            when(hostF.getNumberOfActiveRequests()).thenReturn(53+i);
            List<Host> hosts = Arrays.asList(hostX, hostY, hostZ, hostF);
            LoadBalancer lb = new LoadBalancer(hosts, new PeakFactor());
            Host hostPicked = lb.handleRequest(null);
            Assert.assertEquals(50+i,hostPicked.getNumberOfActiveRequests());
        }
    }
}