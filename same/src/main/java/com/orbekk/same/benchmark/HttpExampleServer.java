package com.orbekk.same.benchmark;

import java.util.logging.Logger;

import com.orbekk.same.http.JettyServerBuilder;
import com.orbekk.same.http.JettyServerContainer;

public class HttpExampleServer {
    private final static Logger logger =
            Logger.getLogger(HttpExampleServer.class.getName());
    private volatile JettyServerContainer server;
    
    class ServiceImpl implements HttpExampleService {
        @Override public String methodA(String message, int arg1, int arg2) {
            return message + arg1 + arg2;
        }
    }
    
    public void runServer(int port) throws Exception {
        server = new JettyServerBuilder(port)
            .withService(new ServiceImpl(), HttpExampleService.class)
            .build();
        server.start();
    }
    
    public void stopServer() throws Exception {
        server.stop();
    }
}
