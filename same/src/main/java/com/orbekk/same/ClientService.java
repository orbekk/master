package com.orbekk.same;

public interface ClientService {
    void notifyNetwork(String networkName, String masterUrl) throws Exception;
    
    void setState(String component, String data, long revision) throws Exception;
    
    // Manual discovery request by client.
    void discoveryRequest(String remoteUrl) throws Exception;
}
