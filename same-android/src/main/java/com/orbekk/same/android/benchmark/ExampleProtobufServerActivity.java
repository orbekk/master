package com.orbekk.same.android.benchmark;

import android.app.Activity;
import android.os.Debug;

import com.orbekk.same.benchmark.ClientBenchmark;
import com.orbekk.same.benchmark.ExampleServer;

public class ExampleProtobufServerActivity extends Activity {
    public final ExampleServer server = new ExampleServer();
    
    @Override public void onResume() {
        super.onResume();
        server.runServer(12000);
        try {
            ClientBenchmark.benchmark("localhost", 12000, 100, 2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Override public void onStop() {
        super.onStop();
        server.stopServer();
    }
}
