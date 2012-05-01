package com.orbekk.same.apps;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.same.Services;

public class GetSystemStatus {
    final String location;
    
    public GetSystemStatus(String location) {
        this.location = location;
    }
    
    public class StatusPrinter implements RpcCallback<Services.SystemStatus> {
        @Override public void run(Services.SystemStatus status) {
            if (status == null) {
                System.err.println("Could not retrieve status.");
                return;
            }
            System.out.println("=================================================");
            System.out.println("System status for " + location);
            System.out.println(status);
            System.out.println("=================================================");
        }
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
            system.getSystemStatus(rpc, Services.Empty.getDefaultInstance(),
                    new StatusPrinter());
            rpc.await();
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: " + GetSystemStatus.class + " host:port");
            System.exit(1);
        }
        String location = args[0];
        try {
            new GetSystemStatus(location).run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
