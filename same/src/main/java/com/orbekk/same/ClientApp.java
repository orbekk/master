package com.orbekk.same;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.net.HttpUtil;
import com.orbekk.same.http.RpcHandler;

public class ClientApp {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;
    private static final int timeout = 1000;
    
    public void run(int port, String networkName,
            String masterUrl) {
        SameController controller = SameController.create(null);
        try {
            controller.start();
        } catch (Exception e) {
            logger.error("Failed to start Same", e);
        }
        controller.joinNetwork(masterUrl);
        controller.join();
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
