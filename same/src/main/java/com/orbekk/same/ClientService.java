package com.orbekk.same;

public interface ClientService {
    void notifyNetwork(String networkName, String masterUrl);
    
    void setState(String component, String data, long revision);
}
