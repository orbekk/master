package com.orbekk.same;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class MasterApp {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;
    private static final int timeout = 1000;    
    
    public void run(int port) {
        ConnectionManagerImpl connections = new ConnectionManagerImpl(timeout,
                timeout);
        State state = new State("MasterNetwork");
        Broadcaster broadcaster =
                BroadcasterImpl.getDefaultBroadcastRunner();
        MasterServiceImpl master = new MasterServiceImpl(state, connections,
                broadcaster);
        JsonRpcServer jsonServer = new JsonRpcServer(master, MasterService.class);
        server = new Server(port);
        RpcHandler rpcHandler = new RpcHandler(jsonServer, master);
        server.setHandler(rpcHandler);
        
        Thread masterThread = new Thread(master);
        masterThread.start();
        
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Could not start jetty server: {}", e);
        }
        
        try {
            server.join();
            masterThread.join();
        } catch (InterruptedException e) {
            logger.info("Received exception. Exiting. {}", e);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: port");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        (new MasterApp()).run(port);
    }
}
