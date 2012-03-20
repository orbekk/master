package com.orbekk.same;

public interface ClientService {
    void notifyNetwork(String networkName, String masterUrl) throws Exception;

    void setState(String component, String data, long revision) throws Exception;

    // Manual discovery request by client.
    void discoveryRequest(String remoteUrl) throws Exception;
    
    /** A new master takes over.
     * 
     * @param masterUrl The new master URL.
     * @param masterId The ID of the new master. Only accept if this is higher
     *      than the current master.
     */
    void masterTakeover(String masterUrl, String networkName,
            int masterId) throws Exception;
    
    /** The master is down, so start a new master election. */
    void masterDown() throws Exception;
}
