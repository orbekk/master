package com.orbekk.same;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.R;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SameControllerActivity extends Activity { 
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Messenger sameService = null;
    private List<String> networkNames = new ArrayList<String>();
    private List<String> networkUrls = new ArrayList<String>();
    
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
        public synchronized void onReceive(Context context, Intent intent) {
            if (SameService.AVAILABLE_NETWORKS_UPDATE.equals(intent.getAction())) {
                networkNames = intent.getStringArrayListExtra(
                        SameService.AVAILABLE_NETWORKS);
                networkUrls = intent.getStringArrayListExtra(
                        SameService.NETWORK_URLS);
                updateNetworkList();
            }
        }
    };

    private AdapterView.OnItemClickListener networkListClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String networkName = networkNames.get(position);
            int networkIndex = networkNames.indexOf(networkName);
            String masterUrl = networkUrls.get(networkIndex);
            joinNetwork(masterUrl);      
        }
    };
    
    private void updateNetworkList() {
        ListView list = (ListView)findViewById(R.id.network_list);
        list.setAdapter(new ArrayAdapter<String>(
                SameControllerActivity.this,
                R.layout.list_text_item, networkNames));        
    }
    
    
    public void createNetwork(View unused) {
        Message message = Message.obtain(null, SameService.CREATE_NETWORK);
        try {
            sameService.send(message);
        } catch (RemoteException e) {
            logger.error("Failed to create network", e);
            throw new RuntimeException(e);
        }
    }
    
    public void joinNetworkUrl(View unused) {
        String masterUrl = "";
        Intent intent = new Intent(this, SameService.class);
        intent.setAction("join");
        EditText t = (EditText)findViewById(R.id.master_service_url);
        masterUrl = t.getText().toString();
        if (!masterUrl.startsWith("http://")) {
            masterUrl = "http://" + masterUrl;
        }
        if (!masterUrl.endsWith("/MasterService.json")) {
            masterUrl += "/MasterService.json";
        }
        joinNetwork(masterUrl);
    }
    
    private void joinNetwork(String masterUrl) {
        logger.info("joinNetwork({})", masterUrl);
        Message message = Message.obtain(null, SameService.JOIN_NETWORK);
        message.getData().putString("masterUrl", masterUrl);
        try {
            sameService.send(message);
        } catch (RemoteException e) {
            logger.error("Failed to send message", e);
            throw new RuntimeException(e);
        }
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
        
        ListView networkList = (ListView)findViewById(R.id.network_list);
        networkList.setOnItemClickListener(networkListClickListener);
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

