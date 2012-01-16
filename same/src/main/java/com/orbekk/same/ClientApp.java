package com.orbekk.same;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class ClientApp {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;
    
    public void run(int port, String networkName) {
        ConnectionManagerImpl connections = new ConnectionManagerImpl();
        State state = new State(networkName);
        Broadcaster broadcaster =
                BroadcasterImpl.getDefaultBroadcastRunner(connections);
        MasterServiceImpl master = new MasterServiceImpl(state, connections,
                broadcaster);
        ClientServiceImpl client = new ClientServiceImpl(state, connections);
        
        JsonRpcServer jsonServer = new JsonRpcServer(client, ClientService.class);
        server = new Server(port);
        RpcHandler rpcHandler = new RpcHandler(jsonServer, client);
        server.setHandler(rpcHandler);
        
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Could not start jetty server: {}", e);
        }
        
        try {
            server.join();
        } catch (InterruptedException e) {
            logger.info("Received exception. Exiting. {}", e);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: port networkName");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String networkName = args[1];
        (new ClientApp()).run(port, networkName);
    }
}
