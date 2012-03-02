package com.orbekk.same.directory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.orbekk.same.discovery.DirectoryService;

public class DirectoryServiceImpl implements DirectoryService {
    public final static long EXPIRE_TIME = 15 * 60l * 1000;  // 15 minutes
    List<NetworkEntry> networkList = new ArrayList<NetworkEntry>();
    
    synchronized void cleanNetworkList() {
        long expiredTime = System.currentTimeMillis() - EXPIRE_TIME;
        for (Iterator<NetworkEntry> it = networkList.iterator(); it.hasNext();) {
            NetworkEntry e = it.next();
            if (e.hasExpired(expiredTime)) {
                it.remove();
            }
        }
    }

    @Override
    public List<String> getNetworks() throws Exception {
        cleanNetworkList();
        List<String> networks = new ArrayList<String>();
        for (NetworkEntry e : networkList) {
            networks.add(e.networkName);
            networks.add(e.masterUrl);
        }
        return networks;
    }

    @Override
    public void registerNetwork(String networkName, String masterUrl)
            throws Exception {
        cleanNetworkList();
        NetworkEntry entry = new NetworkEntry(networkName, masterUrl);
        entry.register(System.currentTimeMillis());
        networkList.remove(entry);
        networkList.add(entry);
    }
}
