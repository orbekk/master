package com.orbekk.same.http;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class JettyServerBuilder {
    Logger logger = LoggerFactory.getLogger(getClass());
    int port;
    ServletContextHandler context = null;    

    public JettyServerBuilder(int port) {
        this.port = port;
    }

    public JettyServerBuilder withServlet(HttpServlet servlet, String pathSpec) {
        logger.info("Servlet binding: {} → {}", pathSpec, servlet);
        getServletContextHandler().addServlet(new ServletHolder(servlet),
                pathSpec);
        return this;
    }

    public <T> JettyServerBuilder withService(T service, Class<T> clazz) {
        JsonRpcServer server = new JsonRpcServer(service, clazz);
        String pathSpec = "/" + clazz.getSimpleName() + ".json";
        return withServlet(new RpcServlet(server), pathSpec);
    }

    public JettyServerContainer build() {
        JettyServerContainer server = JettyServerContainer.create(port); 
        server.setReuseAddress(true);
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
