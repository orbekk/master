package com.orbekk.same.http;

import java.util.ArrayList;

import javax.servlet.http.HttpServlet;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class TjwsServerBuilder {
    private int port;
    private ArrayList<String> servletPaths = new ArrayList<String>();
    private ArrayList<HttpServlet> servlets = new ArrayList<HttpServlet>();
    
    public TjwsServerBuilder(int port) {
        this.port = port;
    }
    
    /** Note: Does not preserve order. */
    public TjwsServerBuilder withServlet(HttpServlet servlet,
            String pathSpec) {
        servletPaths.add(pathSpec);
        servlets.add(servlet);
        return this;
    }
    
    public <T> TjwsServerBuilder withService(T service, Class<T> clazz) {
        JsonRpcServer server = new JsonRpcServer(service, clazz);
        String pathSpec = "/" + clazz.getSimpleName() + ".json";
        return withServlet(new RpcServlet(server), pathSpec);
    }
    
    public TjwsServerContainer build() {
        TjwsServerContainer server = TjwsServerContainer.create(port);
        for (int i = 0; i < servletPaths.size(); i++) {
            String path = servletPaths.get(i);
            HttpServlet servlet = servlets.get(i);
            server.addServlet(path, servlet);
        }
        return server;
    }
}
