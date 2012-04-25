package com.orbekk.same.directory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.protobuf.SimpleProtobufServer;

public class DirectoryApp {
    public static final int DISCOVERY_PORT = 15072;
    
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(DirectoryApp.class);
        SimpleProtobufServer server =
                SimpleProtobufServer.create(DISCOVERY_PORT);
        server.registerService(new DirectoryServiceImpl());
        server.start();
    }
}
