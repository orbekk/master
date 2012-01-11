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
    void participateNetwork(String networkName, String clientId,
            String url);

    /**
     * Notification of participation in network.
     */
    void notifyParticipation(String networkName, String masterId,
            Map<String, String> participants);
}
