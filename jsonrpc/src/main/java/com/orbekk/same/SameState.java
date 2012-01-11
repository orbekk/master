package com.orbekk.same;

import java.util.List;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of a 'Same' state.
 *
 * This class manages the current state of the Same protocol.
 */
public class SameState extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConnectionManager connections;
    private List<String> participants = new LinkedList<String>();
    private String currentState = "";
    private String networkName;
    // The client id of this participant.
    private String clientId;
    private boolean stopped = false;

    /**
     * Queue for pending participants.
     */
    private List<String> pendingParticipants = new LinkedList<String>();

    public SameState(String networkName, String clientId,
            ConnectionManager connections) {
        this.networkName = networkName;
        this.clientId = clientId;
        this.connections = connections;
    }

    public synchronized List<String> getParticipants() {
        return participants;
    }

    public String getClientId() {
        return clientId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getCurrentState() {
        return currentState;
    }

    public synchronized void addParticipant(String clientId, String url) {
        synchronized(this) {
            logger.info("Add pending participant: {} ({})", clientId, url);
            pendingParticipants.add(url);
            notifyAll();
        }
    }

    private synchronized void handleNewParticipants() {
        // Adding all pending participants ensures that each of the new
        // participants is informed of all participants.
        //
        // TODO: Does not inform old participants.
        participants.addAll(pendingParticipants);
        for (String url : pendingParticipants) {
            logger.info("New participant: {}", url);
            SameService remoteService = connections.getConnection(url);
            remoteService.notifyParticipation(networkName, participants);
        }
        pendingParticipants.clear();
    }

    public synchronized void run() {
        while (!stopped) {
            handleNewParticipants();
            try {
                wait(1000);
            } catch (InterruptedException e) {
                // Ignore interrupt in wait loop.
            }
        }
    }

    public synchronized void stopSame() {
        try {
            stopped = true;
            notifyAll();
            this.join();
        } catch (InterruptedException e) {
            logger.warn("Got InterruptedException while waiting for SameState " +
                    "to finish. Ignoring.");
        }
    }
}
