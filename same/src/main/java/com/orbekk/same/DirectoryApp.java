package com.orbekk.same;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.discovery.DirectoryService;

public class DirectoryApp {
    private static final Logger logger =
            LoggerFactory.getLogger(DirectoryApp.class);
    public static final int CONNECTION_TIMEOUT = 2 * 1000;
    public static final int READ_TIMEOUT = 2 * 1000;
    private String[] args;
    
    public DirectoryApp(String[] args) {
        this.args = args;
    }
    
    public void run() {
        ConnectionManager connections = new ConnectionManagerImpl(
                CONNECTION_TIMEOUT, READ_TIMEOUT);
        DirectoryService directory = connections.getDirectory(args[0]);
        try {
            List<String> networks = directory.getNetworks();
            System.out.println("Available networks:");
            System.out.println(networks);
            System.out.println("Registering network.");
            directory.registerNetwork("InvalidNetwork", "InvalidUrl");
            System.out.println("Available networks:");
            System.out.println(directory.getNetworks());
        } catch (Exception e) {
            logger.error("Unable to contact directory service.", e);
        }
    }
    
    public static void main(String[] args) {
        new DirectoryApp(args).run();
    }
}
