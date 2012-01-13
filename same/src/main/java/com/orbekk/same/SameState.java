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

    // TODO: Change the name of State.
    private com.orbekk.same.State state =
            new com.orbekk.same.State();

    /**
     * The client id of this participant.
     */
    private String clientId;

    /**
     * Stopping condition for this thread.
     */
    private boolean stopped = false;

    private String _setState = null;
    private Map<String, String> _setParticipants = null;

    private Map<String, String> pendingParticipants =
            new HashMap<String, String>();

    public SameState(String networkName, String clientId,
            ConnectionManager connections) {
        state.setNetworkName(networkName);
        this.clientId = clientId;
        this.connections = connections;
        state.setMasterId(clientId);
        state.getParticipants().put(clientId, null);
    }

    public String getMasterId() {
        return state.getMasterId();
    }

    public synchronized Map<String, String> getParticipants() {
        return state.getParticipants();
    }

    /**
     * Reset this SameService to an initial state.
     *
     * TODO: Implement fully.
     */
    private synchronized void resetState() {
        state = new com.orbekk.same.State();
        pendingParticipants.clear();
    }

    public synchronized void joinNetwork(String networkName, String masterId) {
        resetState();
        state.setNetworkName(networkName);
        state.setMasterId(masterId);
        logger.info("Joined network {}.", networkName);
    }

    public String getClientId() {
        return clientId;
    }

    public String getNetworkName() {
        return state.getNetworkName();
    }

    public String getCurrentState() {
        return state.getData();
    }

    /**
     * TODO: Move to a separate library.
     */
    public void librarySetNewState(String newState) {
        connections.getConnection(
                state.getParticipants().get(state.getMasterId()))
                .setState(newState);
    }

    public String getUrl() {
        return state.getParticipants().get(clientId);
    }

    @Override
    public void setUrl(String url) {
        logger.info("My URL is {}", url);
        state.getParticipants().put(clientId, url);
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
                state.getParticipants().clear();
                state.getParticipants().putAll(_setParticipants);
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
            state.setData(_setState);
            _setState = null;
        }
    }

    private boolean isMaster() {
        return state.getMasterId().equals(clientId);
    }

    private synchronized void handleNewParticipants() {
        if (!isMaster()) {
            for (Map.Entry<String, String> e : pendingParticipants.entrySet()) {
                SameService master = connections.getConnection(
                        state.getParticipants().get(state.getMasterId()));
                logger.info("Redirecting participant request to {}",
                        state.getMasterId());
                String clientId = e.getKey();
                String url = e.getValue();
                master.participateNetwork(state.getNetworkName(), clientId,
                        url);
            }
        } else {
            state.getParticipants().putAll(pendingParticipants);
            for (Map.Entry<String, String> e :
                    pendingParticipants.entrySet()) {
                String clientId = e.getKey();
                String url = e.getValue();
                logger.info("New participant: {} URL({})", clientId, url);
                SameService remoteService = connections.getConnection(url);
                remoteService.notifyParticipation(state.getNetworkName(),
                        state.getMasterId());
                broadcast(new ServiceOperation(){
                    @Override void run(SameService service) {
                        service.setParticipants(state.getParticipants());
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
        for (Map.Entry<String, String> e :
                state.getParticipants().entrySet()) {
            String clientId = e.getKey();
            String url = e.getValue();
            if (!clientId.equals(this.clientId)) {
                operation.run(connections.getConnection(url));
            }
        }
    }
}
