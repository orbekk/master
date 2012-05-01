/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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