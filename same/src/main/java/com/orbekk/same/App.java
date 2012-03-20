package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.config.Configuration;

public class App {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void run(String[] args) {
        Configuration configuration = Configuration.loadOrDie();
        SameController controller = SameController.create(configuration);
        try {
            controller.start();
            controller.searchNetworks();
            if ("true".equals(configuration.get("isMaster"))) {
                controller.createNetwork(configuration.get("networkName"));
            } else {
                controller.joinNetwork(configuration.get("masterUrl"));
            }
            controller.join();
        } catch (Exception e) {
            logger.error("Error in App.", e);
        }
    }

    public static void main(String[] args) {
        new App().run(args);
    }
}
