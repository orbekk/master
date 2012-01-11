package com.orbekk.rpc;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.ConnectionManagerImpl;
import com.orbekk.same.SameState;
import com.orbekk.same.SameService;
import com.orbekk.same.SameServiceImpl;
import org.eclipse.jetty.server.Server;

public class App {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Arguments: port networkName clientId");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String networkName = args[1];
        String clientId = args[2];

        ConnectionManagerImpl connections = new ConnectionManagerImpl();

        SameState sameState = new SameState(networkName, clientId,
                connections);
        sameState.start();

        SameServiceImpl service = new SameServiceImpl(sameState);
        JsonRpcServer jsonServer = new JsonRpcServer(service,
                SameService.class);   
    
        Server server = new Server(port);
        RpcHandler rpcHandler = new RpcHandler(jsonServer, service);
        server.setHandler(rpcHandler);

        try {
            server.start();
        } catch (Exception e) {
            System.out.println("Could not start jetty server.");
            e.printStackTrace();
        }
        
        try {
            server.join();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        }
    }
}
