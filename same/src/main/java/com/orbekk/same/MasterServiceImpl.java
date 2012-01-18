package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.State.Component;

public class MasterServiceImpl implements MasterService, UrlReceiver, Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConnectionManager connections;
    private State state;
    private boolean stopped = false;
    private Broadcaster broadcaster;
    private List<String> _fullStateReceivers = new ArrayList<String>();
    
    public MasterServiceImpl(State initialState, ConnectionManager connections,
            Broadcaster broadcaster) {
        state = initialState;
        this.connections = connections;
        this.broadcaster = broadcaster;
    }
    
    @Override
    public void joinNetworkRequest(String clientUrl) {
        logger.info("JoinNetworkRequest({})", clientUrl);
        List<String> participants = participants();
        if (!participants.contains(clientUrl)) {
            participants.add(clientUrl);
            _fullStateReceivers.add(clientUrl);
            synchronized(this) {
                state.updateFromObject(".participants", participants,
                        state.getRevision(".participants") + 1);
                notifyAll();
            }
        } else {                
            logger.warn("Client {} already part of network. " +
                    "Ignoring participation request", clientUrl);
        }
    }
    
    public boolean _sendUpdatedComponents() {
        boolean worked = false;
        for (final Component component : state.getAndClearUpdatedComponents()) {
            logger.info("Broadcasting new component {}", component);
            broadcastNewComponents(participants(), listWrap(component));
            worked = true;
        }
        return worked;
    }
    
    private <T>List<T> listWrap(T o) {
        List<T> list = new ArrayList<T>();
        list.add(o);
        return list;
    }
    
    public synchronized boolean _sendFullState() {
        boolean hasWork = _fullStateReceivers.size() != 0;
        if (hasWork) {
            final List<State.Component> components = state.getComponents();
            broadcastNewComponents(participants(), components);
            _fullStateReceivers.clear();
        }
        return hasWork;
    }
    
    private synchronized void removeParticipant(String url) {
        logger.error("Remove participant {}: Operation not supported", url);
    }
    
    private void broadcastNewComponents(List<String> destinations,
            final List<State.Component> components) {
        broadcaster.broadcast(destinations, new ServiceOperation() {
            @Override public void run(String url) {
                 ClientService client = connections.getClient(url);
                 try {
                     for (Component c : components) {
                         client.setState(c.getName(), c.getData(),
                                 c.getRevision());
                     }
                 } catch (Exception e) {
                     logger.warn("Client {} failed to receive state update.");
                     removeParticipant(url);
                 }
            }
        });
    }
    
    private List<String> participants() {
        return state.getList(".participants");
    }
    
    
    @Override
    public synchronized boolean updateStateRequest(String component,
            String newData, long revision) {
        boolean updated = state.update(component, newData, revision + 1);
        if (updated) {
            notifyAll();
        }
        return updated;
    }

    @Override
    public void setUrl(String url) {
        String myUrl = url + "MasterService.json";
        logger.info("Master URL is " + myUrl);
        state.update(".masterUrl", myUrl, 1);
    }

    boolean _performWork() {
        boolean worked = false;
        worked |= _sendUpdatedComponents();
        worked |= _sendFullState();
        return worked;
    }
    
    @Override
    public void run() {
        while (!stopped) {
            if (_performWork()) {
                synchronized (this) {
                    try {
                        wait(500);
                    } catch (InterruptedException e) {
                        // Ignore interrupt in wait loop.
                    }
                }
            }
        }
    }
}
