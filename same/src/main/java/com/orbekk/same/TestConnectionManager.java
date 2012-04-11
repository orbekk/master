package com.orbekk.same;

import java.util.Map;
import java.util.HashMap;

import com.orbekk.paxos.PaxosService;
import com.orbekk.same.Services.Directory;
import com.orbekk.same.Services.Master;

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
    public Map<String, Services.Directory> directoryMap =
            new HashMap<String, Services.Directory>();
    public Map<String, Services.Master> masterMap0 =
            new HashMap<String, Services.Master>();

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
    public Directory getDirectory(String location) {
        return directoryMap.get(location);
    }

    @Override
    public Master getMaster0(String location) {
        return masterMap0.get(location);
    }
}
