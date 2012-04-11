package com.orbekk.same;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.NewRpcChannel;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.same.Services.Directory;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.MasterState;
import com.orbekk.same.Services.NetworkDirectory;

public class DirectoryApp {
    private static final Logger logger =
            LoggerFactory.getLogger(DirectoryApp.class);
    public static final int CONNECTION_TIMEOUT = 2 * 1000;
    public static final int READ_TIMEOUT = 2 * 1000;
    private String[] args;
    
    public DirectoryApp(String[] args) {
        this.args = args;
    }
    
    private void addTestNetwork(Directory directory) throws InterruptedException {
        final Rpc rpc = new Rpc();
        MasterState request = MasterState.newBuilder()
                .setNetworkName("Test network")
                .setMasterUrl("invalid:invalid")
                .build();
        RpcCallback<Empty> done = new RpcCallback<Empty>() {
            @Override public void run(Empty unused) {
            }
        };
        directory.registerNetwork(rpc, request, done);
        rpc.await();
        if (rpc.isOk()) {
            System.out.println("Added network.");
        }
    }
    
    private void listNetworks(Directory directory) throws InterruptedException {
        final Rpc rpc = new Rpc();
        RpcCallback<NetworkDirectory> done =
                new RpcCallback<NetworkDirectory>() {
            @Override public void run(NetworkDirectory directory) {
                if (rpc.failed()) {
                    System.err.println("Failed to get network list.");
                } else {
                    System.out.println("Networks:");
                    for (Services.MasterState network :
                        directory.getNetworkList()) {
                        System.out.println(network.getNetworkName() + "\t" +
                                network.getMasterUrl());
                    }
                }
            }
        };
        directory.getNetworks(rpc, Empty.getDefaultInstance(), done);
        rpc.await();
    }
    
    public void run() throws InterruptedException {
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        NewRpcChannel channel = null;
        try {
            channel = NewRpcChannel.create(host, port);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            System.exit(1);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        Services.Directory directory = Services.Directory.newStub(channel);
        addTestNetwork(directory);
        listNetworks(directory);
        channel.close();
    }
    
    public static void main(String[] args) {
        try {
            new DirectoryApp(args).run();
        } catch (InterruptedException e) {
            throw new AssertionError("Should not be interrupted.");
        }
    }
}
