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
        logger.error("SetState not yet implemented.");
    }

    @Override
    public void setUrl(String myUrl) {
        logger.info("My URL is {}.", myUrl);
        this.myUrl = myUrl;
    }
}
