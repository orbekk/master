package com.orbekk.same;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.config.Configuration;

public class MasterApp {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public void run(Configuration configuration) {
        SameController controller = SameController.create(configuration);
        try {
            controller.start();
        } catch (Exception e) {
            logger.error("Failed to start Same", e);
        }
        controller.join();
    }
    
    public static void main(String[] args) {
        Configuration configuration = Configuration.loadOrDie();
        (new MasterApp()).run(configuration);
    }
}
