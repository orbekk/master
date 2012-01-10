package com.orbekk.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.log4j.Logger;

public class BroadcastListener {
    private int port;
    private Logger logger = Logger.getLogger(getClass());
    
    public BroadcastListener(int port) {
        this.port = port;
    }
    
    public boolean listen() {
        logger.info("Waiting for broadcast on port " + port);
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            logger.warn("Failed to create socket.", e.fillInStackTrace());
            return true;
        }
        try {
            socket.setBroadcast(true);
        } catch (SocketException e) {
            logger.warn(e.fillInStackTrace());
        }
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            logger.warn(e.fillInStackTrace());
        }
        logger.info("Received broadcast from " + packet.getAddress() +
                ": " + new String(packet.getData()));
        return true;
    }
    
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        BroadcastListener listener = new BroadcastListener(port);
        System.out.println("Received broadcast: " + listener.listen());
    }
}
