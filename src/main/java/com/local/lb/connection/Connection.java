package com.local.lb.connection;

import com.local.lb.servlet.properties.Transport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class Connection implements AutoCloseable {
    private final Logger LOGGER = LogManager.getLogger(this);
    private String url;
    private String content;
    private Transport transport;
    private UUID uuid;
    private final ConnectionPool connectionPool;

    Connection(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    ConnectionBuilder getConnectionBuilder() {
        return new Connection(connectionPool).new ConnectionBuilder();
    }

    @Override
    public void close() {
        LOGGER.info("CONNECTION " + uuid + " IS CLOSED AND IS BACK TO THE POOL");
        this.uuid = null;
        this.transport = null;
        this.url = null;
        this.content = null;
        connectionPool.releaseConnection(this);
    }

    class ConnectionBuilder {

        ConnectionBuilder setUrl(String url) {
            Connection.this.url = url;
            return this;
        }

        ConnectionBuilder setContent(String content) {
            Connection.this.content = content;
            return this;
        }

        ConnectionBuilder setTransport(Transport transport) {
            Connection.this.transport = transport;
            return this;
        }

        ConnectionBuilder setConnectionId(UUID id) {
            Connection.this.uuid = id;
            return this;
        }

        Connection buildConnection() {

            return Connection.this;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public Transport getTransport() {
        return transport;
    }

    public UUID getUuid() {
        return uuid;
    }

}
