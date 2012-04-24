package com.orbekk.same.benchmark;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.protobuf.Rpc;
import com.orbekk.same.benchmark.Example.Data;

public class ClientBenchmark {
    private final Example.Service service;
    private final int warmupIterations;
    private final int iterations;
    
    public static void benchmark(String host, int port, int warmupIterations,
            int iterations) throws InterruptedException {
        RpcChannel channel = null;
        try {
            channel = RpcChannel.create(host, port);
            Example.Service service = Example.Service.newStub(channel);
            ClientBenchmark benchmark = new ClientBenchmark(
                    service, warmupIterations, iterations);
            benchmark.benchmark();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
    }
    
    public ClientBenchmark(Example.Service service,
            int warmupIterations, int iterations) {
        this.service = service;
        this.warmupIterations = warmupIterations;
        this.iterations = iterations;
    }
    
    private void runBenchmark(int iterations) throws InterruptedException {
        final CountDownLatch finished =
                new CountDownLatch(iterations);
        
        for (int i = 0; i < iterations; i++) {
            Example.Data request = Example.Data.newBuilder()
                    .setArg1(i).build();
            Rpc rpc = new Rpc();
            service.methodA(rpc, request, new RpcCallback<Example.Data>() {
                @Override
                public void run(Data ignored) {
                    finished.countDown();
                }
            });
        }
        finished.await();
    }
    
    public void benchmark() throws InterruptedException {
        long warmupStart = System.currentTimeMillis();
        runBenchmark(warmupIterations);
        long warmupFinished = System.currentTimeMillis();
        System.out.println("Warmup: " + warmupIterations + " in " +
                (warmupFinished - warmupStart) + "ms. ");
        long start = System.currentTimeMillis();
        runBenchmark(iterations);
        long finished = System.currentTimeMillis();
        System.out.println("Benchmark: " + iterations+ " in " +
                (finished - start) + "ms. ");
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: ClientBenchmark <host> <port>");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.valueOf(args[1]);
        try {
            benchmark(host, port, 2000, 10000);
        } catch (InterruptedException e) {
            System.out.println("Benchmark failed.");
        }
    }
}
