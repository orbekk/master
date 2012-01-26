package com.orbekk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.discovery.SameService;
import com.orbekk.net.Broadcaster;
import com.orbekk.same.ClientApp;
import com.orbekk.same.ClientServiceImpl;
import com.orbekk.same.SameInterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HelloAndroidActivity extends Activity { 
    private Logger logger = LoggerFactory.getLogger(getClass());
   
    public void createNetwork(View unused) {
        logger.info("Creating network");
        Intent intent = new Intent(this, SameService.class);
        intent.setAction("create");
        startService(intent);
    }
    
    public void joinNetwork(View unused) {
        logger.info("Joining network");
        Intent intent = new Intent(this, SameService.class);
        intent.setAction("join");
        startService(intent);
    }
    
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

        setContentView(R.layout.main);        
        
//		ClientApp client = new ClientApp();
//		SameInterface client_ = client.getClient(10015, "ClientNetwork",
//		        "http://10.0.0.6:10010/");
//		GameController controller = GameController.create(
//		        GameController.newPlayer(), client_);
//		setContentView(new GameView(this, controller));

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

