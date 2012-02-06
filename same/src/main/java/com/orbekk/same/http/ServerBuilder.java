package com.orbekk.same.http;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class ServerBuilder {
    Logger logger = LoggerFactory.getLogger(getClass());
    int port;
    ServletContextHandler context = null;    
    
    public ServerBuilder(int port) {
        this.port = port;
    }
    
    public ServerBuilder withServlet(HttpServlet servlet, String pathSpec) {
        logger.info("Servlet binding: {} → {}", pathSpec, servlet);
        getServletContextHandler().addServlet(new ServletHolder(servlet),
                pathSpec);
        return this;
    }
    
    public <T> ServerBuilder withService(T service, Class<T> clazz) {
        JsonRpcServer server = new JsonRpcServer(service, clazz);
        String pathSpec = "/" + clazz.getSimpleName() + ".json";
        return withServlet(new RpcServlet(server), pathSpec);
    }
    
    public ServerContainer build() {
        ServerContainer server = ServerContainer.create(port); 
        server.setContext(getServletContextHandler());
        return server;
    }

    private ServletContextHandler getServletContextHandler() {
        if (context == null) {
            context = new ServletContextHandler();
            context.setContextPath("/");
        }
        return context;
    }
}
