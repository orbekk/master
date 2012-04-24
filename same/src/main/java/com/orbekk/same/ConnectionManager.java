package com.orbekk.same;

import com.orbekk.paxos.PaxosService;

/**
 * An interface that returns a connection for a participant.
 *
 * When testing, this interface can be mocked to use local participants only.
 */
public interface ConnectionManager {
    Services.Master getMaster0(String location);
    Services.Client getClient0(String location);
    Services.Directory getDirectory(String location);
    Services.Paxos getPaxos0(String location);
}
