package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientServiceImpl implements ClientService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void notifyNetwork(String networkName, String masterUrl) {
        logger.error("NotifyNetwork not yet implemented.");
    }

    @Override
    public void setState(String component, String data, long revision) {
        logger.error("SetState not yet implemented.");
    }
}
