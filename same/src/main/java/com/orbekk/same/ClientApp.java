package com.orbekk.same;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.net.HttpUtil;

public class ClientApp {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;
    private static final int timeout = 1000;
    
    public ClientService getClient(int port, String networkName,
            String masterUrl) {
        logger.info("Starting client with port:{}, networkName:{}, masterUrl:{}",
                new Object[]{port, networkName, masterUrl});
        ConnectionManagerImpl connections = new ConnectionManagerImpl(timeout,
                timeout);
        State state = new State(networkName);
        Broadcaster broadcaster =
                BroadcasterImpl.getDefaultBroadcastRunner();
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
        
        while (client.getUrl() == null) {
            HttpUtil.sendHttpRequest(masterUrl + "ping?port=" + port);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore interrupt in wait loop.
            }
        }
        
        client.joinNetwork(masterUrl + "MasterService.json");
        return client;
    }
    
    public void run(int port, String networkName,
            String masterUrl) {
        getClient(port, networkName, masterUrl);
        try {
            server.join();
        } catch (InterruptedException e) {
            logger.warn("Interrupted.", e);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: port networkName masterUrl");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String networkName = args[1];
        String masterUrl = args[2];
        (new ClientApp()).run(port, networkName, masterUrl);
        
    }
}
