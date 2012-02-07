package com.orbekk;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.discovery.Broadcaster;
import com.orbekk.discovery.SameService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SameControllerActivity extends Activity { 
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
        // InetAddress address = new Broadcaster(this).getBroadcastAddress();
        EditText t = (EditText)findViewById(R.id.editText1);
        intent.putExtra("masterUrl", t.getText().toString());
        startService(intent);
    }
    
    private void showIpAddress() {
        TextView t = (TextView)findViewById(R.id.ipAddress);
        t.setText("My IP: ");
        t.append(new Broadcaster(this).getWlanAddress().getHostAddress());
    }
    
    private void showBroadcastAddress() {
    	EditText t = (EditText)findViewById(R.id.editText1);
    	t.setText(new Broadcaster(this).getBroadcastAddress().getHostAddress());
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
        showIpAddress();
        // showBroadcastAddress();
        
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

