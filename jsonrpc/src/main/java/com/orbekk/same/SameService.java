package com.orbekk.same;

import java.util.List;

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
    void participateNetwork(String networkName, int remotePort);

    /**
     * Notification of participation in network.
     */
    void notifyParticipation(String networkName, List<String> participants);
}
