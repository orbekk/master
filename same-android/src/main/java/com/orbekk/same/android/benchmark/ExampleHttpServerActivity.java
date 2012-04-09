package com.orbekk.same.android.benchmark;

import android.app.Activity;

import com.orbekk.same.benchmark.ExampleServer;
import com.orbekk.same.benchmark.HttpClientBenchmark;
import com.orbekk.same.benchmark.HttpExampleServer;

public class ExampleHttpServerActivity extends Activity {
    public final HttpExampleServer server = new HttpExampleServer();
    
    @Override public void onResume() {
        super.onResume();
        try {
            server.runServer(12000);
            HttpClientBenchmark.benchmark(
                    "http://localhost:12000/HttpExampleService.json",
                    100, 2000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    @Override public void onStop() {
        super.onStop();
        try {
            server.stopServer();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
