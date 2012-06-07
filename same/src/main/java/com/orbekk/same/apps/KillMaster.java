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
package com.orbekk.same.apps;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.same.Services;
import com.orbekk.same.Services.Empty;

public class KillMaster {
    final String location;
    
    public KillMaster(String location) {
        this.location = location;
    }
    
    public void run() throws Exception {
        RpcChannel channel = null;
        try {
            String host = location.split(":")[0];
            int port = Integer.valueOf(location.split(":")[1]);
            channel = RpcChannel.create(host, port);
            Services.SystemService system = Services.SystemService.newStub(channel);
            Rpc rpc = new Rpc();
            rpc.setTimeout(10000);
            RpcCallback<Empty> done = new RpcCallback<Empty>() {
                @Override public void run(Empty unused) {
                }
            };
            system.killMaster(rpc, Services.Empty.getDefaultInstance(),
                    done);
            rpc.await();
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: " + KillMaster.class + " host:port");
            System.exit(1);
        }
        String location = args[0];
        try {
            new KillMaster(location).run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
