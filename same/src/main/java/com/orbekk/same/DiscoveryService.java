package com.orbekk.same;

import java.net.DatagramPacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.net.BroadcastListener;

public class DiscoveryService extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    BroadcastListener broadcastListener;
    DiscoveryListener listener;

    public DiscoveryService(DiscoveryListener listener,
            BroadcastListener broadcastListener) {
        this.listener = listener;
        this.broadcastListener = broadcastListener;
    }

    @Override
    public void run() {
        logger.info("DiscoveryService starting.");
        while (!Thread.interrupted()) {
            DatagramPacket packet = broadcastListener.listen();
            if (packet == null) {
                // An error or interrupt occurred.
                continue;
            }
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
        logger.info("DiscoveryService stopped.");
    }

    @Override public void interrupt() {
        logger.info("Interrupt()");
        super.interrupt();
        broadcastListener.interrupt();
    }
}
