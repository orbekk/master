package com.orbekk.same.benchmark;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import com.google.protobuf.RpcCallback;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.orbekk.net.MyJsonRpcHttpClient;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.same.benchmark.Example.Data;

public class HttpClientBenchmark {
    private final HttpExampleService service;
    private final int warmupIterations;
    private final int iterations;
    
    public static void benchmark(String url, int warmupIterations,
            int iterations) throws InterruptedException {
        MyJsonRpcHttpClient client;
        try {
            client = new MyJsonRpcHttpClient(
                    new URL(url), 2000, 2000);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        HttpExampleService service = ProxyUtil.createProxy(
                HttpClientBenchmark.class.getClassLoader(),
                HttpExampleService.class,
                client);
        HttpClientBenchmark benchmark = new HttpClientBenchmark(
                service, warmupIterations, iterations);
        benchmark.benchmark();
    }
    
    public HttpClientBenchmark(HttpExampleService service,
            int warmupIterations, int iterations) {
        this.service = service;
        this.warmupIterations = warmupIterations;
        this.iterations = iterations;
    }
    
    private void runBenchmark(int iterations) throws InterruptedException {
        final CountDownLatch finished =
                new CountDownLatch(iterations);
        
        for (int i = 0; i < iterations; i++) {
            service.methodA("", iterations, 0);
            finished.countDown();
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
        if (args.length < 1) {
            System.err.println("Usage: ClientBenchmark <url>");
            System.exit(1);
        }
        String url = args[0];
        try {
            benchmark(url, 1000, 10000);
        } catch (InterruptedException e) {
            System.out.println("Benchmark failed.");
        }
    }
}
