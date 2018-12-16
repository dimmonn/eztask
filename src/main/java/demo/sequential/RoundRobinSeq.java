package demo.sequential;

import com.local.lb.LoadBalancer;
import com.local.lb.balancing.Balancable;
import com.local.lb.balancing.algorythm.RoundRobiin;
import com.local.lb.connection.Connection;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class RoundRobinSeq {
    private static final List<Host> hosts = Arrays.asList(new Host("TEST_1"),
            new Host("TEST_2"),
            new Host("TEST_3"),
            new Host("TEST_4"));
    private static final Logger LOGGER = LogManager.getLogger(RoundRobinSeq.class);

    public static void main(String[] args) {
        runSeqTask(new RoundRobiin());
    }

    private static void runSeqTask(Balancable algorythm) {

        LoadBalancer balancer = new LoadBalancer(hosts, algorythm);
        ConnectionPool connectionPool = new ConnectionPool(10);
        balancer.setConnectionPool(connectionPool);
        for (int i = 0; i < 20; i++) {
            try (Connection connection = connectionPool.
                    getConnection("http://example.com", "testContent", Transport.TCP)) {
                Request request = balancer.getRequestById(connection.getUuid().toString());
                balancer.handleRequest(request);
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
            }

        }
    }
}
