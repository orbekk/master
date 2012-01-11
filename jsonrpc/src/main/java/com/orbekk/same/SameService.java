package com.orbekk.same;

import java.util.Map;

public interface SameService {
    /**
     * A notification that 'networkName' exists.
     *
     * This is called by any participant of a network after a broadcast
     * has been performed.
     */
    void notifyNetwork(String networkName);

    /**
     * A request from the callee to participate in 'networkName'.
     */
    void participateNetwork(String networkName, String clientId, String url);

    /**
     * Notification of participation in network.
     */
    void notifyParticipation(String networkName, String masterId);

    /**
     * New state.
     * 
     * When sent to a non-master from the master, use 'newState' as the
     * current state.
     *
     * When sent to a master, broadcast the new state to all clients.
     */
    void setState(String newState);

    /**
     * Notify all nodes of network participants.
     *
     * Only sent from master to non-master.
     */
    void setParticipants(Map<String, String> participants);
}
