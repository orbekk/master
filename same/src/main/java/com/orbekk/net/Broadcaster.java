package com.orbekk.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Broadcaster {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> broadcastAddresses = new LinkedList<InetAddress>();
        
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.warn("Network problem?", e.fillInStackTrace());
            return broadcastAddresses;
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            try {
                if (iface.isLoopback()) {
                    logger.debug("Ignoring looback device " + iface.getName());
                    continue;
                }
                for (InterfaceAddress address : iface.getInterfaceAddresses()) {
                    InetAddress broadcast = address.getBroadcast();
                    if (broadcast != null) {
                        broadcastAddresses.add(broadcast);
                    }
                }
            } catch (SocketException e) {
                logger.info("Ignoring interface " + iface.getName(), e.fillInStackTrace());
            }
        }
        return broadcastAddresses;
    }

    public boolean sendBroadcast(int port, byte[] data) {
        boolean successful = false;
        for (InetAddress broadcastAddress : getBroadcastAddresses()) {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, port);
                socket.send(packet);
                successful = true;
            } catch (SocketException e) {
                logger.warn("Failed to send broadcast to " + broadcastAddress +
                        ". ", e.fillInStackTrace());
            } catch (IOException e) {
                logger.warn("Error when sending broadcast to " +
                        broadcastAddress + ".", e.fillInStackTrace());
            }
        }
        return successful;
    }
    
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Broadcaster broadcaster = new Broadcaster();
        String message = "Broadcast from Java broadcaster.";
        broadcaster.sendBroadcast(port, message.getBytes());
    }
}
