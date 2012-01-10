package com.orbekk;

import com.orbekk.net.Broadcaster;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class HelloAndroidActivity extends Activity {

    private static String TAG = "master";
    private PingServer pingServer;
    
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        
		Log.i(TAG, "onCreate");
        setContentView(R.layout.main);

        pingServer = PingServer.createPingServer(10080);
        try {
            pingServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        
//        Broadcast broadcast = new Broadcast(this);
//        broadcast.sendBroadcast("Broadcast test".getBytes(), 10010);
        Broadcaster broadcaster = new Broadcaster();
        Log.i(TAG, "Broadcast success: " + broadcaster.sendBroadcast(10010, "Broadcast test from Android".getBytes()));
    }
    
    @Override
    protected void onDestroy() {
        pingServer.stop();
        super.onDestroy();
    }
}

