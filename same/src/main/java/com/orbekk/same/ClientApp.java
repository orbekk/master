package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.config.Configuration;

public class ClientApp {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public void run(Configuration configuration) {
        String networkName = configuration.get("networkName");
        String masterUrl = configuration.get("masterUrl");
        SameController controller = SameController.create(configuration);
        try {
            controller.start();
        } catch (Exception e) {
            logger.error("Failed to start Same", e);
        }
        controller.joinNetwork(masterUrl);
        controller.join();
    }
    
    public static void main(String[] args) {
        Configuration configuration = Configuration.loadOrDie();
        (new ClientApp()).run(configuration);
    }
}
