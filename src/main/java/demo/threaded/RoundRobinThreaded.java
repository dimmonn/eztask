package demo.threaded;

import com.local.lb.LoadBalancer;
import com.local.lb.balancing.algorythm.PeakFactor;
import com.local.lb.balancing.algorythm.RoundRobiin;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import demo.GenericSeqRunner;
import demo.sequential.RoundRobinSeq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
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
        GenericSeqRunner genericSeqRunner = new GenericSeqRunner(new PeakFactor());
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        GenericSeqRunner.registerMBeans(server, hosts);
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
            for (int i = 0; i < 2000; i++) {
                GenericSeqRunner genericSeqRunner = new GenericSeqRunner(new RoundRobiin());
                order.addAll(genericSeqRunner.oneTimeRun(connectionPool, balancer));
            }
            return order;
        }, executorService);
    }

}
