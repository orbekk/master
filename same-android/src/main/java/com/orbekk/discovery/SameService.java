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

public class SameService extends Service {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Thread discoveryThread = null;
    
    public final class DiscoveryThread extends Thread {
        Broadcast broadcast;
        
        public DiscoveryThread() {
            broadcast = new Broadcast(SameService.this);
        }
        
        @Override public void run() {
            while (!Thread.interrupted()) {
                byte[] data = new byte[1024];
                DatagramPacket packet = broadcast.receiveBroadcast(15066);
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
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service start: " + intent.getAction(),
                Toast.LENGTH_SHORT).show();
        if (discoveryThread == null) {
            synchronized (this) {
                discoveryThread = new DiscoveryThread();
                discoveryThread.start();
            }
        }
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
        discoveryThread.interrupt();
    }

}
