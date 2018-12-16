package com.local.lb.connection;

import com.local.lb.LbConnectionListener;
import com.local.lb.servlet.properties.Transport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {

    private final LinkedBlockingDeque<Connection> connections = new LinkedBlockingDeque<>();

    private final List<LbConnectionListener> listeners = new ArrayList<>();
    private Logger LOGGER = LogManager.getLogger(this);

    public ConnectionPool(int pool) {
        for (int i = 0; i < pool; i++) {
            connections.add(new Connection(this));
        }
    }


    public synchronized Connection getConnection(String url, String content, Transport transport) {
        Connection connection;
        blockThreadUntillConnectionAvailable();
        connection = Objects.requireNonNull(connections.pollLast()).
                getConnectionBuilder().
                setUrl(url).
                setContent(content).
                setTransport(transport).
                setConnectionId(UUID.randomUUID()).
                buildConnection();
        if (connection != null) {
            updateLbContainer(url, content, transport, connection.getUuid());
        }
        return connection;
    }

    private void blockThreadUntillConnectionAvailable() {
        while (connections.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.info(e.getMessage());
            }
        }
    }

    private void updateLbContainer(String url, String content, Transport transport, UUID uuid) {
        for (LbConnectionListener listener : listeners) {
            listener.onConnectionEstablished(url, content, transport, uuid);
        }
    }

    public synchronized void releaseConnection(Connection connection) {
        notify();
        connections.add(connection);
    }

    public void attach(LbConnectionListener lbConnectionListener) {
        listeners.add(lbConnectionListener);
    }

    public int availableConnections() {
        return connections.size();
    }

}
