package com.orbekk.same;

import com.orbekk.paxos.PaxosService;
import com.orbekk.same.discovery.DirectoryService;

/**
 * An interface that returns a connection for a participant.
 *
 * When testing, this interface can be mocked to use local participants only.
 */
public interface ConnectionManager {
    ClientService getClient(String url);
    MasterService getMaster(String url);
    PaxosService getPaxos(String url);
    DirectoryService getDirectory(String url);
}
