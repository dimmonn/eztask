package demo.threaded;

import com.local.lb.LoadBalancer;
import com.local.lb.balancing.algorythm.RoundRobiin;
import com.local.lb.connection.Connection;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;
import demo.sequential.RoundRobinSeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RoundRobinThreaded {
    private static final Logger LOGGER = LogManager.getLogger(RoundRobinSeq.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private static final ConnectionPool connectionPool = new ConnectionPool(10);
    private static final List<Host> hosts = Arrays.asList(new Host("TEST_1"),
            new Host("TEST_2"),
            new Host("TEST_3"),
            new Host("TEST_4"));

    public static void main(String[] args) {
        LoadBalancer balancer = new LoadBalancer(hosts, new RoundRobiin());
        balancer.setConnectionPool(connectionPool);
        CompletableFuture.allOf(generateTask(balancer),
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

    private static CompletableFuture<List<Host>> generateTask(LoadBalancer balancer) {
        return CompletableFuture.supplyAsync(() -> {
            List<Host> order = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                try (Connection connection = connectionPool.
                        getConnection("http://example.com", "testContent", Transport.TCP)) {
                    Request request = balancer.getRequestById(connection.getUuid().toString());
                    order.add(balancer.handleRequest(request));
                } catch (Exception e) {
                    LOGGER.info(e.getMessage());
                }
            }
            return order;
        }, executorService);
    }

}
