/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orbekk.same.android;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.same.Services;
import com.orbekk.same.android.net.Networking;
import com.orbekk.same.android.widget.NetworkListAdapter;

public class SameControllerActivity extends Activity { 
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Messenger sameService = null;
    private volatile Services.NetworkDirectory networks = null;
    
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

    private AdapterView.OnItemClickListener networkListClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Services.MasterState network = networks.getNetwork(position);
            joinNetwork(network.getMasterLocation());      
        }
    };
    
    private void updateNetworkList() {
        Services.NetworkDirectory currentNetworks = networks;
        if (currentNetworks == null) {
            return;
        }
        ListView list = (ListView)findViewById(R.id.network_list);
        list.setAdapter(new NetworkListAdapter(
                SameControllerActivity.this,
                R.layout.list_text_item, currentNetworks));        
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
    
    public void killMaster(View unused) {
        Message message = Message.obtain(null, SameService.KILL_MASTER);
        try {
            sameService.send(message);
        } catch (RemoteException e) {
            logger.error("Failed to kill master", e);
            throw new RuntimeException(e);
        }
    }
    
    public void joinNetworkUrl(View unused) {
        Intent intent = new Intent(this, SameService.class);
        intent.setAction("join");
        EditText t = (EditText)findViewById(R.id.master_service_url);
        String masterUrl = t.getText().toString();
        joinNetwork(masterUrl);
    }
    
    private void joinNetwork(String masterUrl) {
        logger.info("joinNetwork({})", masterUrl);
        Message message = Message.obtain(null, SameService.JOIN_NETWORK);
        message.getData().putString("masterLocation", masterUrl);
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
        t.append(new Networking(this).getWlanAddress().getHostAddress());
    }
    
    public void doneClicked(View unused) {
        finish();
    }
    
    public void searchNetworks(View unused) {
        logger.info("Looking up networks.");
        final Toast failedToast = Toast.makeText(this, "No networks found!", Toast.LENGTH_SHORT);
        RpcChannel channel = null;
        try {
            channel = RpcChannel.create(
                    SameService.DIRECTORY_HOST, SameService.DIRECTORY_PORT);
        } catch (UnknownHostException e) {
            logger.warn("Could not retrieve networks.", e);
            e.printStackTrace();
            return;
        } catch (IOException e) {
            failedToast.show();
            e.printStackTrace();
            return;
        }
        Services.Directory directory = Services.Directory.newStub(channel);
        if (directory == null) {
            logger.warn("No discovery service configured.");
            return;
        }
        final Rpc rpc = new Rpc();
        rpc.setTimeout(10000);
        RpcCallback<Services.NetworkDirectory> done =
                new RpcCallback<Services.NetworkDirectory>() {
            @Override public void run(Services.NetworkDirectory networks) {
                if (!rpc.isOk()) {
                    failedToast.show();
                    logger.warn("Unable to find networks: {}", rpc.errorText());
                    return;
                }
                setAvailableNetworks(networks);
            }
        };
        directory.getNetworks(rpc, Services.Empty.getDefaultInstance(), done);
    }
    
    private void setAvailableNetworks(final Services.NetworkDirectory networks) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                SameControllerActivity.this.networks = networks;
                updateNetworkList();
            }
        });
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
        
        searchNetworks(null);
    }
    
    @Override public void onStop() {
        super.onStop();
        if (sameService != null) {
            unbindService(sameConnection);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

