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
    private String _setState = null;

    private String networkName;

    /**
     * The master participant id.
     */
    private String masterId;

    public String getMasterId() {
        return this.masterId;
    }

    /**
     * The participants of this network.
     *
     * Maps clientId to url.
     */
    private Map<String, String> participants = new HashMap<String, String>();

    /**
     * New participants.
     *
     * New participants map to replace participants.
     */
    private Map<String, String> _setParticipants = null;

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
        networkName = "";
        masterId = "";
        pendingParticipants.clear();
        participants.clear();
    }

    public synchronized void joinNetwork(String networkName, String masterId) {
        resetState();
        this.networkName = networkName;
        this.masterId = masterId;
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

    /**
     * TODO: Move to a separate library.
     */
    public void librarySetNewState(String newState) {
        connections.getConnection(participants.get(masterId))
                .setState(newState);
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

    public synchronized void setParticipants(Map<String, String> participants) {
        logger.info("Pending operation: _setParticipants");
        _setParticipants = participants;
        notifyAll();
    }

    public synchronized void setState(String newState) {
        logger.info("Pending operation: _setState");
        _setState = newState;
        notifyAll();
    }

    private synchronized void handleSetParticipants() {
        if (_setParticipants != null) {
            if (isMaster()) {
                logger.error("{}: Master received setParticipants.", clientId);
            } else {
                logger.info("{}: New participants committed.", clientId);
                participants = _setParticipants;
            }
        }
        _setParticipants = null;
    }

    public synchronized void handleSetState() {
        if (_setState != null) {
            if (isMaster()) {
                broadcast(new ServiceOperation() {
                    @Override void run(SameService service) {
                        service.setState(_setState);
                    }
                });
            }
            currentState = _setState;
            _setState = null;
        }
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
                remoteService.notifyParticipation(networkName, masterId);
                broadcast(new ServiceOperation(){
                    @Override void run(SameService service) {
                        service.setParticipants(participants);
                    }
                });
            }
        }
        pendingParticipants.clear();
    }

    /**
     * This method runs the pending commands to SameState.
     *
     * It should be called by the worker thread, but can be called directly
     * for testing purposes to avoid threading in unit tests.
     */
    synchronized void internalRun() {
        handleNewParticipants();
        handleSetState();
        handleSetParticipants();
    }

    public synchronized void run() {
        while (!stopped) {
            internalRun();
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

    public abstract static class ServiceOperation {
        abstract void run(SameService service);
    }
    
    public synchronized void broadcast(ServiceOperation operation) {
        for (Map.Entry<String, String> e : participants.entrySet()) {
            String clientId = e.getKey();
            String url = e.getValue();
            if (!clientId.equals(this.clientId)) {
                operation.run(connections.getConnection(url));
            }
        }
    }
}
