package demo;

import com.local.lb.LoadBalancer;
import com.local.lb.balancing.algorythm.PeakFactor;
import com.local.lb.connection.Connection;
import com.local.lb.connection.ConnectionPool;
import com.local.lb.model.Host;
import com.local.lb.servlet.Balancer;
import com.local.lb.servlet.Request;
import com.local.lb.servlet.properties.Transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class Application {

    public static void main(String[] args) {
        String content = "this message shall be loadbalanced";
        List<Host> hosts = new ArrayList<>();
        hosts.add(new Host("test1"));
        hosts.add(new Host("test2"));
        hosts.add(new Host("test3"));
        hosts.add(new Host("test4"));
        LoadBalancer balancer = new LoadBalancer(hosts, new PeakFactor());
        ConnectionPool connectionPool = new ConnectionPool(10);
        balancer.setConnectionPool(connectionPool);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(11);

        for (int i = 0; i < 11; i++) {
            Callable<Boolean> task = () -> {
                runDemo(content, balancer, connectionPool);
                return true;

            };
            executorService.schedule(task, new Random().nextInt(100), TimeUnit.MILLISECONDS);
        }

        executorService.shutdown();

    }


    private static void runDemo(String content, Balancer balancer, ConnectionPool connectionPool) {
        for (int i = 0; i < 11; i++) {

            try (Connection connection = connectionPool.
                    getConnection("http://example.com", content, Transport.TCP)) {
                Request request = ((LoadBalancer) balancer).getRequestById(connection.getUuid().toString());
                balancer.handleRequest(request);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
