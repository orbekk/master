package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientServiceImpl implements ClientService, UrlReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConnectionManager connections;
    private State state;
    private String myUrl = null;

    public ClientServiceImpl(State state, ConnectionManager connections) {
        this.state = state;
        this.connections = connections;
    }

    @Override
    public void notifyNetwork(String networkName, String masterUrl) {
        logger.error("NotifyNetwork not yet implemented.");
    }

    @Override
    public void setState(String component, String data, long revision) {
        boolean status = state.update(component, data, revision);
        if (!status) {
            logger.warn("Ignoring update: {}",
                    new State.Component(component, revision, data));
        }
    }

    @Override
    public void setUrl(String myUrl) {
        this.myUrl = myUrl + "ClientService.json";
        logger.info("My URL is {}.", this.myUrl);
    }
    
    public String getUrl() {
        return myUrl;
    }
    
    public void joinNetwork(String masterUrl) {
        if (myUrl != null) {
            MasterService master = connections.getMaster(masterUrl);
            state.clear();
            master.joinNetworkRequest(myUrl);          
        } else {
            logger.error("Tried to join network at {}, but my url is unknown. " +
                    "Run discovery service.", masterUrl);
        }
    }
    
    State testGetState() {
        return state;
    }
}
