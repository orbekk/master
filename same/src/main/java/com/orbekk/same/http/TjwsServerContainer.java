package com.orbekk.same.http;

import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import Acme.Serve.Serve;

public class TjwsServerContainer {
    private static class MyServer extends Serve {
        public void join() {
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                return;
            };
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    private MyServer server;
    
    public static TjwsServerContainer create(int port) {
        MyServer server = new MyServer();
        server.setAttribute(Serve.ARG_PORT, port);
        return new TjwsServerContainer(server);
    }
    
    public TjwsServerContainer(MyServer server) {
        this.server = server;
    }
    
    public int getPort() {
        return (Integer)this.server.getAttribute(Serve.ARG_PORT);
    }
    
    public void start() {
        server.runInBackground();
    }
    
    public void stop() {
        server.stopBackground();
    }
    
    public void join() {
        server.join();
    }
}
