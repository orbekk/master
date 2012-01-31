package com.orbekk.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class Broadcaster {
    private Context context;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private DatagramSocket socket = null;
    
    public Broadcaster(Context context) {
        this.context = context;
    }
    
    public InetAddress fromInt(int ip) {
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
          quads[k] = (byte) ((ip >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            logger.warn("Failed to find broadcast address.");
            return null;
        }
    }
    
    public DhcpInfo getDhcpInfo() {
        WifiManager wifi =
                (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wifi.getDhcpInfo();
    }
    
    public synchronized InetAddress getWlanAddress() {
        return fromInt(getDhcpInfo().ipAddress);
    }
    
    public synchronized InetAddress getBroadcastAddress() {
        DhcpInfo dhcp = getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        return fromInt(broadcast);
    }
    
    public synchronized boolean sendUdpData(byte[] data, InetAddress ip, int port) {
        try {
            socket = new DatagramSocket(0);
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
            socket.send(packet);
            return true;
        } catch (SocketException e) {
            logger.warn("Failed to send broadcast.", e.fillInStackTrace());
            return false;
        } catch (IOException e) {
            logger.warn("Error when sending broadcast.", e.fillInStackTrace());
            return false;
        } finally {
            socket.close();
            socket = null;
        }
    }
    
    public synchronized DatagramPacket receiveBroadcast(int port) {
        try {
            socket = new DatagramSocket(port);
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            return packet;
        } catch (IOException e) {
            logger.warn("Failed to receive broadcast.", e);
            return null;
        } finally {
            socket.close();
            socket = null;
        }
    }
    
    public void interrupt() {
        if (socket != null) {
            socket.close();
        }
    }
}