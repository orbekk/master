package com.orbekk.same.http;

import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerContainer {
    Logger logger = LoggerFactory.getLogger(getClass());
    Server server;
    int port;
    ServletContextHandler context = null;

    public ServerContainer(Server server, int port, ServletContextHandler context) {
        this.server = server;
        this.port = port;
        this.context = context;
    }

    public static ServerContainer create(int port) {
        Server server = new Server(port);
        return new ServerContainer(server, port, null);
    }

    public void setContext(ServletContextHandler context) {
        server.setHandler(context);
        this.context = context;
    }

    public void setReuseAddress(boolean on) {
        Connector connector = server.getConnectors()[0];
        if (connector instanceof AbstractConnector) {
            AbstractConnector connector_ = (AbstractConnector)connector;
            connector_.setReuseAddress(on);
        }
    }

    public int getPort() {
        if (port == 0) {
            return server.getConnectors()[0].getLocalPort();
        } else {
            return port;
        }
    }

    public void start() throws Exception {
        server.start();
        logger.info("Started server on port {}", getPort());
    }

    public void stop() throws Exception {
        server.stop();
        logger.info("Server stopped.");
    }

    public void join() throws InterruptedException {
        server.join();
    }
}
