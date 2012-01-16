package com.orbekk.same;

public class TestConnectionManager implements ConnectionManager {
    public Map<String, ClientService> clientMap =
        new HashMap<String, ClientService>();
    public Map<String, MasterService> masterMap =
        new HashMap<String, MasterService>();

    public ClientService getClient(String url) {
        return clientMap.get(url);
    }

    public MasterService getMaster(String url) {
        return masterMap.get(url);
    }
}
