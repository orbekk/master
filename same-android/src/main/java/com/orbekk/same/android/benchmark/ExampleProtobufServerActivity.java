package com.orbekk.same.android.benchmark;

import android.app.Activity;

import com.orbekk.same.benchmark.ExampleServer;

public class ExampleProtobufServerActivity extends Activity {
    public final ExampleServer server = new ExampleServer();
    
    @Override public void onResume() {
        super.onResume();
        server.runServer(12000);
    }
    
    @Override public void onStop() {
        super.onStop();
        server.stopServer();
    }
}
