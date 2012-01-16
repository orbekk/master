package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.State.Component;

public class MasterServiceImpl implements MasterService, UrlReceiver, Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConnectionManager connections;
    private State state;
    private boolean stopped = false;
    private Broadcaster broadcaster;
    private List<String> _fullStateReceivers = new ArrayList<String>();
    
    public MasterServiceImpl(State initialState, ConnectionManager connections,
            Broadcaster broadcaster) {
        state = initialState;
        this.broadcaster = broadcaster;
    }
    
    @Override
    public void joinNetworkRequest(String networkName, String clientUrl) {
        if (networkName.equals(state.getDataOf(".networkName"))) {
            List<String> participants = participants();
            if (!participants.contains(clientUrl)) {
                participants.add(clientUrl);
                synchronized(this) {
                    notifyAll();
                    state.updateFromObject(".participants", participants,
                            state.getRevision(".participants"));
                }
            } else {                
                logger.warn("Client {} already part of network. " +
                        "Ignoring participation request", clientUrl);
            }
        } else {
            logger.warn("Client {} tried to join {}, but network name is {}",
                    new Object[]{ clientUrl, networkName, 
                            state.getDataOf(".networkName") });
        }
    }
    
    public boolean _sendUpdatedComponents() {
        boolean worked = false;
        for (final String component : state.getAndClearUpdatedComponents()) {
            logger.info("Broadcasting new component {}", state.show(component));
            broadcaster.broadcast(participants(), new ServiceOperation() {
               @Override public void run(ClientService client) {
                   client.setState(component, state.getDataOf(component),
                           state.getRevision(component));
               }
            });
            worked = true;
        }
        return worked;
    }
    
    public boolean _sendFullState() {
        boolean worked = _fullStateReceivers.size() != 0;
        final List<State.Component> components = state.getComponents();
        broadcaster.broadcast(participants(), new ServiceOperation() {
            @Override public void run(ClientService client) {
                for (Component c : components) {
                    client.setState(c.getName(), c.getData(), c.getRevision());
                }
            }
        });
        return worked;
    }
    
    private List<String> participants() {
        return state.getList(".participants");
    }
    
    
    @Override
    public synchronized boolean updateStateRequest(String component,
            String newData, long revision) {
        boolean updated = state.update(component, newData, revision);
        if (updated) {
            notifyAll();
        }
        return updated;
    }

    @Override
    public void setUrl(String url) {
        String myUrl = url + "MasterService.json";
        logger.info("Master URL is " + myUrl);
        state.update(".masterUrl", myUrl, 0);
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
