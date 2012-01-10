package com.orbekk;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class Broadcast {
    private Context context;
    private Logger logger = Logger.getLogger(getClass());
    
    public Broadcast(Context context) {
        this.context = context;
    }
    
    public InetAddress getBroadcastAddress() {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
          quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            logger.warn("Failed to find broadcast address.");
            return null;
        }
    }
    
    public boolean sendBroadcast(byte[] data, int port) {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(data, data.length, getBroadcastAddress(), port);
            socket.send(packet);
            return true;
        } catch (SocketException e) {
            logger.warn("Failed to send broadcast.", e.fillInStackTrace());
            return false;
        } catch (IOException e) {
            logger.warn("Error when sending broadcast.", e.fillInStackTrace());
            return false;
        }
    }
}