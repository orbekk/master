package com.orbekk.same;

import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.net.BroadcastListener;

public class DiscoveryService extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    BroadcastListener broadcastListener;
    DiscoveryListener listener;
    
    public DiscoveryService(int port, DiscoveryListener listener,
            BroadcastListener broadcastListener) {
        this.listener = listener;
        this.broadcastListener = broadcastListener;
    }
    
    public void run() {
        while (!Thread.interrupted()) {
            DatagramPacket packet = broadcastListener.listen();
            String content = new String(packet.getData(), 0, packet.getLength());
            String[] words = content.split(" ");
            
            if (!content.startsWith("Discover") || words.length < 2) {
                logger.warn("Invalid discovery message: {}", content);
                continue;
            }
            
            String url = words[1];
            logger.info("Received discovery from {}", url);
            if (listener != null) {
                listener.discover(url);
            }
        }
    }
    
    @Override public void interrupt() {
        super.interrupt();
        broadcastListener.interrupt();
    }
}
