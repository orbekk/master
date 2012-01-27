package com.orbekk.discovery;

import java.net.DatagramPacket;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.ClientApp;
import com.orbekk.same.MasterApp;

public class SameService extends Service {
    final static int PORT = 15066;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Thread discoveryThread = null;
    
    public final class DiscoveryThread extends Thread {
        Broadcaster broadcast;
        
        public DiscoveryThread() {
            broadcast = new Broadcaster(SameService.this);
        }
        
        @Override public void run() {
            while (!Thread.interrupted()) {
                byte[] data = new byte[1024];
                DatagramPacket packet = broadcast.receiveBroadcast(PORT);
                String content = new String(packet.getData(), 0, packet.getLength());
                Message message = Message.obtain();
                message.obj = content;
                toastHandler.sendMessage(message);
            }
        }
        
        @Override public void interrupt() {
            super.interrupt();
            broadcast.interrupt();
        }
    }
    
    private Handler toastHandler = new Handler() {
        @Override public void handleMessage(Message message) {
            Toast.makeText(SameService.this,
                    (String)message.obj, Toast.LENGTH_SHORT)
                            .show();
            logger.info("Display toast: {}", (String)message.obj);
        }
    };
    
    private void createNetwork() {
        if (discoveryThread == null) {
            synchronized (this) {
                discoveryThread = new DiscoveryThread();
                discoveryThread.start();
            }
        }
    }
    
    private void sendBroadcastDiscovery() {
        String message = "Discover " + (PORT + 2);
        byte[] data = message.getBytes();
        new Broadcaster(this).sendBroadcast(data, PORT);        
    }
    
    private void joinNetwork() {
        sendBroadcastDiscovery();
        // new ClientApp().run(PORT+2, "ClientNetwork", null);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service start: " + intent.getAction(),
                Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals("create")) {
            createNetwork();
        } else if (intent.getAction().equals("join")) {
            joinNetwork();
        }
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
        discoveryThread.interrupt();
    }

}
