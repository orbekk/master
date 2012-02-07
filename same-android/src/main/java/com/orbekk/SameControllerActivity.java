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
        EditText t = (EditText)findViewById(R.id.master_service_url);
        intent.putExtra("masterUrl", t.getText().toString());
        startService(intent);
    }
    
    private void showIpAddress() {
        TextView t = (TextView)findViewById(R.id.ipAddress);
        t.setText("My IP: ");
        t.append(new Broadcaster(this).getWlanAddress().getHostAddress());
    }
    
    private void showBroadcastAddress() {
    	EditText t = (EditText)findViewById(R.id.master_service_url);
    	t.setText(new Broadcaster(this).getBroadcastAddress().getHostAddress());
    }
    
    public void doneClicked(View unused) {
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");

        setContentView(R.layout.controller);        
        showIpAddress(); 
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

