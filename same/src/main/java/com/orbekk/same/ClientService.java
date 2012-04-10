package com.orbekk.same;

public interface ClientService {
    void setState(String component, String data, long revision) throws Exception;

    /** A new master takes over.
     * 
     * @param masterUrl The new master URL.
     * @param masterId The ID of the new master. Only accept if this is higher
     *      than the current master.
     */
    void masterTakeover(String masterUrl, String networkName,
            int masterId) throws Exception;
    
    /** The master is down, so start a new master election. */
    void masterDown(int masterId) throws Exception;
}
