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
     *
     * A client may not know its URL. If the url parameter is empty,
     * use info from the HttpServletRequest.
     *
     * TODO: Always pass a valid URL and get rid of the port parameter.
     */
    void participateNetwork(String networkName, String clientId,
            String url, int remotePort);

    /**
     * Notification of participation in network.
     */
    void notifyParticipation(String networkName, List<String> participants);
}
