package com.orbekk.same.benchmark;

import java.util.logging.Logger;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.protobuf.SimpleProtobufServer;
import com.orbekk.same.benchmark.Example.Data;

public class ExampleServer {
    private final static Logger logger =
            Logger.getLogger(ExampleServer.class.getName());
    private volatile SimpleProtobufServer server;
    
    class ServiceImpl extends Example.Service {
        @Override
        public void methodA(RpcController controller, Data request, RpcCallback<Data> done) {
            Data response = Data.newBuilder()
                .setMessage(request.getMessage())
                .setArg1(request.getArg1())
                .setArg2(request.getArg2())
                .build();
            done.run(response);
        }
    }
    
    public void runServer(int port) {
        server = SimpleProtobufServer.create(port);
        server.registerService(new ServiceImpl());
        server.start();
        logger.info("Running SimpleProtobufServer on port " + server.getPort());
    }
    
    public void stopServer() {
        server.interrupt();
        logger.info("Server stopped.");
    }
    
    public static void main(String[] args) {
        ExampleServer server = new ExampleServer();
        server.runServer(12000);
    }
}
