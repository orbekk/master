package com.orbekk.same.discovery;

import java.util.List;

/**
 * Maintains a registry of available networks.
 * 
 * The discovery service is only meant to be used for debugging.
 */
public interface DirectoryService {
    /**
     * Returns a list of network names and master urls interleaved, i.e.,
     * 
     *   [NetworkName1, MasterUrl1, ...]
     */
    List<String> getNetworks() throws Exception;
    
    /**
     * Register a network.
     */
    void registerNetwork(String networkName, String masterUrl) throws Exception;
}
