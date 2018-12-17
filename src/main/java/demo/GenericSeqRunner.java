package demo;

import com.local.lb.LoadBalancer;
import com.local.lb.balancing.Balancable;
import com.local.lb.connection.Connection;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class GenericSeqRunner {
    private final Balancable balancable;
    private static final Logger LOGGER = LogManager.getLogger(GenericSeqRunner.class);
    public GenericSeqRunner(Balancable balancable) {
        this.balancable = balancable;
    }

    public void runSeqTask(List<Host> hosts){
        LoadBalancer balancer = new LoadBalancer(hosts, balancable);
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
