package com.orbekk.same;

import java.util.Map;
import java.util.HashMap;

import com.orbekk.paxos.PaxosService;
import com.orbekk.same.discovery.DirectoryService;

/**
 * This class is used in test.
 */
public class TestConnectionManager implements ConnectionManager {
    public Map<String, ClientService> clientMap =
            new HashMap<String, ClientService>();
    public Map<String, MasterService> masterMap =
            new HashMap<String, MasterService>();
    public Map<String, PaxosService> paxosMap =
            new HashMap<String, PaxosService>();
    public Map<String, DirectoryService> directoryMap =
            new HashMap<String, DirectoryService>();

    public TestConnectionManager() {
    }

    @Override
    public ClientService getClient(String url) {
        return clientMap.get(url);
    }

    @Override
    public MasterService getMaster(String url) {
        return masterMap.get(url);
    }

    @Override
    public PaxosService getPaxos(String url) {
        return paxosMap.get(url);
    }

    @Override
    public DirectoryService getDirectory(String url) {
        return directoryMap.get(url);
    }
}
