package com.orbekk.rpc;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.SameService;
import com.orbekk.same.SameServiceImpl;
import org.eclipse.jetty.server.Server;

public class App {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Arguments: port networkName");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String networkName = args[1];

        SameService service = new SameServiceImpl(networkName);
        JsonRpcServer jsonServer = new JsonRpcServer(service,
                SameService.class);   
    
        Server server = new Server(port);
        RpcHandler rpcHandler = new RpcHandler(jsonServer);
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
