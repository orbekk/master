package com.orbekk.discovery;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.ClientApp;
import com.orbekk.same.DiscoveryListener;
import com.orbekk.same.MasterApp;
import com.orbekk.same.NetworkNotificationListener;
import com.orbekk.same.SameController;

public class SameService extends Service {
    final static int PORT = 15066;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Thread discoveryThread = null;
    private SameController sameController = null;
    
    public final class DiscoveryThread extends Thread {
        Broadcaster broadcast;
        DiscoveryListener listener;
        
        public DiscoveryThread(DiscoveryListener listener) {
            broadcast = new Broadcaster(SameService.this);
            this.listener = listener;
        }
        
        @Override public void run() {
            while (!Thread.interrupted()) {
                DatagramPacket packet = broadcast.receiveBroadcast(PORT);
                String content = new String(packet.getData(), 0, packet.getLength());
                String[] words = content.split(" ");
                
                if (!content.startsWith("Discover") || content.length() < 2) {
                    logger.warn("Invalid discovery message: {}", content);
                    continue;
                }
                
                String port = words[1];
                String url = "http://" + packet.getAddress().getHostAddress() +
                        ":" + port + "/ClientService.json";
                listener.discover(url);
                
                Message message = Message.obtain();
                message.obj = "New client: " + url;
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
                discoveryThread = new DiscoveryThread(sameController.getClient());
                discoveryThread.start();
            }
        }
    }
    
    private void sendBroadcastDiscovery(InetAddress ip) {
        Broadcaster broadcaster = new Broadcaster(this);
        String message = "Discover " + (PORT + 2);
        byte[] data = message.getBytes();
        if (ip.equals(broadcaster.getBroadcastAddress())) {
            broadcaster.sendUdpData(data, ip, PORT);
        } else {
            String remoteAddress =
                    String.format("http://%s:%s/ClientService.json",
                    		ip.getHostAddress(), PORT + 2);
            sameController.getClient().sendDiscoveryRequest(
                    remoteAddress);
        }
    }
    
    private void joinNetwork(InetAddress ip) {
        sameController.getClient().setNetworkListener(
                new NetworkNotificationListener() {
                    @Override
                    public void notifyNetwork(String networkName, String masterUrl) {
                        Message message = Message.obtain();
                        message.obj = "notifyNetwork(" + networkName + ", " +
                                    masterUrl;
                        toastHandler.sendMessage(message);
                    }
                });
        sendBroadcastDiscovery(ip);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.setProperty("http.keepAlive", "false");
        Toast.makeText(this, "service start: " + intent.getAction(),
                Toast.LENGTH_SHORT).show();
        if (sameController == null) {
            sameController = SameController.create(PORT + 2);
            try {
                sameController.start();
                String myIp = new Broadcaster(this).getWlanAddress()
                        .getHostAddress();
                String myUrl = "http://" + myIp + ":" + (PORT + 2) + "/";
                sameController.setUrl(myUrl);
            } catch (Exception e) {
                logger.error("Failed to start server", e);
                return START_STICKY;
            }
        }
        if (intent.getAction().equals("create")) {
            createNetwork();
        } else if (intent.getAction().equals("join")) {
        	try {
				InetAddress ip = InetAddress.getByName(intent.getExtras().getString("ip"));
				joinNetwork(ip);
			} catch (UnknownHostException e) {
				logger.error("Unknown host.", e);
			}
        }
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
        if (sameController != null) {
            sameController.stop();
        }
    }

}
