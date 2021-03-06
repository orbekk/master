package com.orbekk.stats;

import com.orbekk.protobuf.SimpleProtobufServer;

public class Server {
    private final Experiment1Impl exp1 = new Experiment1Impl();
    private final Experiment2Impl exp2 = new Experiment2Impl();

    public static void main(String[] args) {
        new Server().run();
    }
    
    public void run() {
        addShutdownHook();
        SimpleProtobufServer server = SimpleProtobufServer.create(
                Common.PORT);
        server.registerService(exp1);
        server.registerService(exp2);
        System.out.println("Waiting for samples...");
        server.start();
    }
    
    public void addShutdownHook() {
        class ShutdownTask implements Runnable {
            @Override public void run() {
                exp1.writeSamples("experiment1.data");
                exp2.writeSamples("experiment2.data");
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask()));
    }
}
