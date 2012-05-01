/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
