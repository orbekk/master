package com.orbekk.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastListener {
    private int port;
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public BroadcastListener(int port) {
        this.port = port;
    }
    
    public DatagramPacket listen() {
        logger.debug("Waiting for broadcast on port " + port);
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            logger.warn("Failed to create socket.", e.fillInStackTrace());
            return null;
        }
        try {
            socket.setBroadcast(true);
        } catch (SocketException e) {
            logger.warn("Exception: {}", e);
        }
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            logger.warn("Exception when listening for broadcast: {}", e);
            return null;
        }
        
        String address = packet.getAddress().getHostAddress();
        logger.debug("Received broadcast from " + address +
                ": " + new String(packet.getData()));
        return packet;
    }
    
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        BroadcastListener listener = new BroadcastListener(port);
        System.out.println("Received broadcast: " + listener.listen());
    }
}
