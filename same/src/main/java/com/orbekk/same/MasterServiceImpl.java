package com.orbekk.same;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServiceImpl implements MasterService, UrlReceiver, Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private State state;
    private boolean stopped = false;
  
    public MasterServiceImpl(State initialState) {
        state = initialState;
    }
    
    @Override
    public void joinNetworkRequest(String networkName, String clientUrl) {
        if (networkName.equals(state.getDataOf(".networkName"))) {
            List<String> participants = state.getList(".participants");
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
        for (String component : state.getAndClearUpdatedComponents()) {
            logger.error("TODO: Send state update for component {}",
                    state.show(component));
            worked = true;
        }
        return worked;
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
