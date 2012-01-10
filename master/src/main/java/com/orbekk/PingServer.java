package com.orbekk;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.rpc.PingService;
import com.orbekk.rpc.PingServiceImpl;
import com.orbekk.rpc.RpcHandler;

public class PingServer {
    Server server;
    Logger logger = Logger.getLogger(getClass());

    public PingServer(Server server) {
        this.server = server;
    }

    public static PingServer createPingServer(int port) {
        PingService service = new PingServiceImpl();
        JsonRpcServer jsonServer = new JsonRpcServer(service, PingService.class);   

        Server server = new Server(port);
        RpcHandler rpcHandler = new RpcHandler(jsonServer);
        server.setHandler(rpcHandler);

        return new PingServer(server);
    }

    public void start() throws Exception {
        logger.info("Starting server.");
        server.start();
    }

    public void join() {
        try {
            server.join();
        } catch (InterruptedException e) {
            logger.info("Received InterruptException while waiting for server.", e.fillInStackTrace());
        }
    }
    
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            logger.warn("Exception when stopping server.", e.fillInStackTrace());
        }
    }
}
