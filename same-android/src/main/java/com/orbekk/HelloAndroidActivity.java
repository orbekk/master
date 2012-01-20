package com.orbekk;

import com.orbekk.net.Broadcaster;
import com.orbekk.same.ClientApp;
import com.orbekk.same.ClientServiceImpl;
import com.orbekk.same.SameInterface;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class HelloAndroidActivity extends Activity {

    private static String TAG = "master";
   
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
        // setContentView(R.layout.main);
        ClientApp client = new ClientApp();
        SameInterface client_ = client.getClient(10015, "ClientNetwork",
                "http://10.0.0.6:10010/");
        GameController controller = GameController.create(
                GameController.newPlayer(), client_);
		setContentView(new GameView(this, controller));
        
//        Broadcast broadcast = new Broadcast(this);
//        broadcast.sendBroadcast("Broadcast test".getBytes(), 10010);
//        Broadcaster broadcaster = new Broadcaster();
//        Log.i(TAG, "Broadcast success: " + broadcaster.sendBroadcast(10010, "Broadcast test from Android".getBytes()));
//        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

