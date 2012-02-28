package com.orbekk.same.http;

import java.util.Properties;

import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Acme.Serve.Serve;

public class TjwsServerContainer implements ServerContainer {
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
        Properties properties = new Properties();
        properties.put(Serve.ARG_PORT, port);
        MyServer server = new MyServer();
        server.arguments = properties;
        return new TjwsServerContainer(server);
    }
    
    public TjwsServerContainer(MyServer server) {
        this.server = server;
    }
    
    /* (non-Javadoc)
     * @see com.orbekk.same.http.ServerContainer#getPort()
     */
    @Override
    public int getPort() {
        return (Integer)this.server.getAttribute(Serve.ARG_PORT);
    }
    
    /* (non-Javadoc)
     * @see com.orbekk.same.http.ServerContainer#start()
     */
    @Override
    public void start() {
        server.runInBackground();
    }
    
    /* (non-Javadoc)
     * @see com.orbekk.same.http.ServerContainer#stop()
     */
    @Override
    public void stop() {
        server.stopBackground();
    }
    
    /* (non-Javadoc)
     * @see com.orbekk.same.http.ServerContainer#join()
     */
    @Override
    public void join() {
        server.join();
    }
    
    public void addServlet(String pathSpec, HttpServlet servlet) {
        server.addServlet(pathSpec, servlet);
    }
}
