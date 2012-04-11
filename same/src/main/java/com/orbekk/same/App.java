package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.config.Configuration;

public class App {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Services.MasterState getMasterInfo(Configuration configuration) {
        return Services.MasterState.newBuilder()
                .setMasterUrl(configuration.get(".masterUrl"))
                .setMasterLocation(configuration.get("masterLocation"))
                .build();
    }
    
    public void run(String[] args) {
        Configuration configuration = Configuration.loadOrDie();
        SameController controller = SameController.create(configuration);
        try {
            controller.start();
            if ("true".equals(configuration.get("isMaster"))) {
                controller.createNetwork(configuration.get("networkName"));
            } else {
                controller.joinNetwork(getMasterInfo(configuration));
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
