package com.orbekk.same.directory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.same.discovery.DirectoryService;
import com.orbekk.same.Services;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.MasterState;
import com.orbekk.same.Services.NetworkDirectory;

public class DirectoryServiceImpl extends Services.Directory implements DirectoryService {
    private Logger logger = LoggerFactory.getLogger(getClass());
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
        logger.info("Cleaned network list. Networks: {}", networkList);
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

    @Override
    public void registerNetwork(RpcController controller, MasterState request,
            RpcCallback<Empty> done) {
        try {
            registerNetwork(request.getNetworkName(), request.getMasterUrl());
        } catch (Exception e) {
            // No RPC call here.
        }
        done.run(Empty.getDefaultInstance());
    }

    @Override
    public void getNetworks(RpcController controller, Empty request,
            RpcCallback<NetworkDirectory> done) {
        NetworkDirectory.Builder directory = NetworkDirectory.newBuilder();
        for (NetworkEntry e : networkList) {
            directory.addNetwork(MasterState.newBuilder()
                    .setMasterUrl(e.masterUrl)
                    .setNetworkName(e.networkName)
                    .build());
        }
        done.run(directory.build());
    }
}
