package com.orbekk.same;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.R;
import com.orbekk.same.android.net.Broadcaster;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SameControllerActivity extends Activity { 
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Messenger sameService = null;
    
    private ServiceConnection sameConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sameService = new Messenger(service);
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            sameService = null;
        }
    };
    
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SameService.AVAILABLE_NETWORKS_UPDATE.equals(intent.getAction())) {
                ArrayList<String> networkList = intent.getStringArrayListExtra(
                    SameService.AVAILABLE_NETWORKS);
                ListView list = (ListView)findViewById(R.id.network_list);
                list.setAdapter(new ArrayAdapter<String>(
                        SameControllerActivity.this,
                        R.layout.list_text_item, networkList));
            }
        }
    };
    
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
    
    public void doneClicked(View unused) {
        finish();
    }
    
    public void searchNetworks(View unused) {
        logger.info("SearchNetworks()");
        Message searchMessage = Message.obtain(null, SameService.SEARCH_NETWORKS);
        try {
            sameService.send(searchMessage);
        } catch (RemoteException e) {
            logger.error("Failed to send message", e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");

        setContentView(R.layout.controller);        
        showIpAddress();
    }
    
    @Override public void onResume() {
        super.onResume();
        
        Intent intent = new Intent(this, SameService.class);
        bindService(intent, sameConnection, Context.BIND_AUTO_CREATE);
        
        IntentFilter sameServiceUpdates = new IntentFilter(
                SameService.AVAILABLE_NETWORKS_UPDATE);
        registerReceiver(broadcastReceiver, sameServiceUpdates);
    }
    
    @Override public void onStop() {
        super.onStop();
        if (sameService != null) {
            unbindService(sameConnection);
        }
        unregisterReceiver(broadcastReceiver);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

