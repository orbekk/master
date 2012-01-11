package com.orbekk.rpc;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.ConnectionManagerImpl;
import com.orbekk.same.SameState;
import com.orbekk.same.SameService;
import com.orbekk.same.SameServiceImpl;
import com.orbekk.net.HttpUtil;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.jetty.server.Server;

public class Client {
    
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Arguments: port clientId thisNetworkName " +
                    "remoteNetworkAddr");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String clientId = args[1];
        String networkName = args[2];
        String remoteAddr = args[3];

        ConnectionManagerImpl connections = new ConnectionManagerImpl();

        SameState sameState = new SameState(networkName, clientId,
                connections);
        sameState.start();

        SameServiceImpl service = new SameServiceImpl(sameState);
        JsonRpcServer jsonServer = new JsonRpcServer(service,
                SameService.class);   
    
        Server server = new Server(port);
        RpcHandler rpcHandler = new RpcHandler(jsonServer, sameState);
        server.setHandler(rpcHandler);

        try {
            server.start();
        } catch (Exception e) {
            System.out.println("Could not start jetty server.");
            e.printStackTrace();
        }

        while (sameState.getUrl() == null) {
            HttpUtil.sendHttpRequest(remoteAddr + "ping?port=" + port);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore interrupt in wait loop.
            }
        }
        
        SameService remoteService = connections.getConnection(remoteAddr);
        remoteService.notifyNetwork("NoNetwork");
        remoteService.participateNetwork("FirstNetwork",
                sameState.getClientId(), sameState.getUrl());

        try {
            server.join();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        }

    }
}
