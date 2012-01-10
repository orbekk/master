package com.orbekk.same;

import java.util.List;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SameServiceImpl implements SameService, CallerInfoListener {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SameState sameState;
    private String currentCallerIp;

    public SameServiceImpl(SameState sameState) {
        this.sameState = sameState;
    }

    @Override
    public void setCaller(String callerIp) {
        currentCallerIp = callerIp;
    }

    @Override
    public void notifyNetwork(String networkName) {
        logger.info("Notification from network " + networkName);
    }

    @Override
    public void participateNetwork(String networkName, int remotePort) {
        if (!networkName.equals(sameState.getNetworkName())) {
            logger.warn("Client tried to join {}, but network name is {}.",
                    networkName, sameState.getNetworkName());
        }
        String url = "http://" + currentCallerIp + ":" + remotePort;
        sameState.addParticipant(url);
    }

    @Override
    public void notifyParticipation(String networkName,
            List<String> participants) {
        logger.info("Joining network {}.", networkName);
        int i = 1;
        for (String participant : participants) {
            logger.info("  {} participant {}: {}",
                    new Object[]{networkName, i, participant});
            i++;
        }
        logger.warn("Joining not implemented.");
    }
}
