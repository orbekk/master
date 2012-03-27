package com.orbekk.same.android.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

public class Networking {
    private Context context;
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    public Networking(Context context) {
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
    
    public synchronized DhcpInfo getDhcpInfo() {
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
}