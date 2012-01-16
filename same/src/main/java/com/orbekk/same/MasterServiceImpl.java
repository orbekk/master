package com.orbekk.same;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServiceImpl implements MasterService, UrlReceiver, Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConnectionManager connections;
    private State state;
    private boolean stopped = false;
    private Broadcaster broadcaster;

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
                }
            } else {                
                logger.warn("Client {} already part of network. " +
                        "Ignoring participation request", clientUrl);
            }
            state.updateFromObject(".participants", participants,
                    state.getRevision(".participants"));
        } else {
            logger.warn("Client {} tried to join {}, but network name is {}",
                    new Object[]{ clientUrl, networkName, 
                            state.getDataOf(".networkName") });
        }
    }
    public boolean _handleJoinNetworkRequests() {
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
    
    private List<String> participants() {
        return state.getList(".participants");
    }
    
    
    @Override
    public boolean updateStateRequest(String component, String newData, long revision) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setUrl(String url) {
        String myUrl = url + "MasterService.json";
        logger.info("Master URL is " + myUrl);
        state.update(".masterUrl", myUrl, 0);
    }

    boolean _performWork() {
        boolean worked = false;
        worked |= _handleJoinNetworkRequests();
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
