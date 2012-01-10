package com.orbekk.rpc;

import org.eclipse.jetty.server.Server;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class App {
    public static void main(String[] args) {
        PingService service = new PingServiceImpl();
        JsonRpcServer jsonServer = new JsonRpcServer(service, PingService.class);   
    
        Server server = new Server(10080);
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
