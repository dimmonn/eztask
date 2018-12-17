package demo.threaded;

import com.local.lb.LoadBalancer;
import com.local.lb.balancing.algorythm.PeakFactor;
import com.local.lb.connection.Connection;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;
import demo.GenericSeqRunner;
import demo.sequential.RoundRobinSeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class PeakFactorThreaded {
    private static final Logger LOGGER = LogManager.getLogger(RoundRobinSeq.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private static final ConnectionPool connectionPool = new ConnectionPool(10);
    private static final List<Host> hosts = Arrays.asList(new Host("TEST_1"),
            new Host("TEST_2"),
            new Host("TEST_3"),
            new Host("TEST_4"));

    public static void main(String[] args) {

        LoadBalancer balancer = new LoadBalancer(hosts, new PeakFactor());
        GenericSeqRunner genericSeqRunner = new GenericSeqRunner(new PeakFactor());
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        GenericSeqRunner.registerMBeans(server, hosts);
        balancer.setConnectionPool(connectionPool);
        CompletableFuture.allOf(
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer),
                generateTask(balancer));
        executorService.shutdown();
    }

    private static CompletableFuture<Map<Host, List<Host>>> generateTask(LoadBalancer balancer) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Host, List<Host>> order = new HashMap<>();
            for (int i = 0; i < 10000; i++) {
                try (Connection connection = connectionPool.
                        getConnection("http://example.com", "testContent", Transport.TCP)) {
                    Request request = balancer.getRequestById(connection.getUuid().toString());
                    order.put(balancer.handleRequest(request), hosts);
                } catch (Exception e) {
                    LOGGER.info(e.getMessage());
                }
            }
            return order;
        }, executorService);
    }
}
