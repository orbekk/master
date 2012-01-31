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
        SameController controller = SameController.create(port);
        try {
            controller.start();
        } catch (Exception e) {
            logger.error("Failed to start Same", e);
        }
        controller.join();
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
