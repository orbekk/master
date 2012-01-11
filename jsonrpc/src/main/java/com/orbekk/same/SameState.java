package com.orbekk.same;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of a 'Same' state.
 *
 * This class manages the current state of the Same protocol.
 */
public class SameState extends Thread implements UrlReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConnectionManager connections;
    private String currentState = "";
    private String networkName;

    /**
     * The master participant id.
     */
    private String masterId;

    /**
     * TODO: Remove.
     */
    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    /**
     * The participants of this network.
     *
     * Maps clientId to url.
     */
    private Map<String, String> participants = new HashMap<String, String>();

    /**
     * The client id of this participant.
     */
    private String clientId;

    /**
     * Stopping condition for this thread.
     */
    private boolean stopped = false;

    private Map<String, String> pendingParticipants =
            new HashMap<String, String>();

    public SameState(String networkName, String clientId,
            ConnectionManager connections) {
        this.networkName = networkName;
        this.clientId = clientId;
        this.connections = connections;
        this.masterId = clientId;
        participants.put(clientId, null);
    }

    /**
     * Get participants as a list.
     *
     * List format: ["clientId1,url1", "clientId2,url2", ...]
     */
    public synchronized Map<String, String> getParticipants() {
        return participants;
    }

    /**
     * Reset this SameService to an initial state.
     *
     * TODO: Implement fully.
     */
    private synchronized void resetState() {
        pendingParticipants.clear();
    }

    public synchronized void joinNetwork(String networkName, String masterId,
            Map<String, String> participants) {
        resetState();
        this.networkName = networkName;
        this.masterId = masterId;
        this.participants = participants;
        logger.info("Joined network {}.", networkName);
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

    public String getUrl() {
        return participants.get(clientId);
    }

    @Override
    public void setUrl(String url) {
        logger.info("My URL is {}", url);
        participants.put(clientId, url);
    }

    public synchronized void addParticipant(String clientId, String url) {
        logger.info("PendingParticipant.add: {} ({})", clientId, url);
        pendingParticipants.put(clientId, url);
        notifyAll();
    }

    private boolean isMaster() {
        return masterId.equals(clientId);
    }

    private synchronized void handleNewParticipants() {
        if (!isMaster()) {
            for (Map.Entry<String, String> e : pendingParticipants.entrySet()) {
                SameService master = connections.getConnection(
                        participants.get(masterId));
                logger.info("Redirecting participant request to {}", masterId);
                String clientId = e.getKey();
                String url = e.getValue();
                master.participateNetwork(networkName, clientId, url);
            }
        } else {
            participants.putAll(pendingParticipants);
            for (Map.Entry<String, String> e :
                    pendingParticipants.entrySet()) {
                String clientId = e.getKey();
                String url = e.getValue();
                logger.info("New participant: {} URL({})", clientId, url);
                SameService remoteService = connections.getConnection(url);
                remoteService.notifyParticipation(networkName, masterId,
                        participants);
            }
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
