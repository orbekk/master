package com.orbekk.same;

import java.util.List;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SameServiceImpl implements SameService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<String> participants = new LinkedList<String>();
    private String networkName;

    public SameServiceImpl(String networkName) {
        this.networkName = networkName;
    }

    @Override
    public void notifyNetwork(String networkName) {
        logger.info("Notification from network " + networkName);
    }

    @Override
    public String participateNetwork(String networkName) {
        logger.info("Got participation request.");
        if (networkName != this.networkName) {
            logger.info("Network name mismatch.");
        }
        return "<Not implemented>";
    }
}
