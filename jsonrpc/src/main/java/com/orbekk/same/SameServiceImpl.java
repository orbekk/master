package com.orbekk.same;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SameServiceImpl implements SameService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SameState sameState;

    public SameServiceImpl(SameState sameState) {
        this.sameState = sameState;
    }

    @Override
    public void notifyNetwork(String networkName) {
        logger.info("Notification from network " + networkName);
    }

    @Override
    public void participateNetwork(String networkName, String clientId,
            String url) {
        if (!networkName.equals(sameState.getNetworkName())) {
            logger.warn("Client tried to join {}, but network name is {}.",
                    networkName, sameState.getNetworkName());
            return;
        }
        if (clientId.equals("") || url.equals("")) {
            logger.warn("Missing client info: ClientId({}), URL({}).",
                    clientId, url);
            return;
        }
        sameState.addParticipant(clientId, url);
    }

    @Override
    public void notifyParticipation(String networkName,
            Map<String, String> participants) {
        logger.info("Joining network {}.", networkName);
        int i = 1;
        for (Map.Entry<String, String> e : participants.entrySet()) {
            String clientId = e.getKey();
            String url = e.getValue();
            logger.info("  {} participant {}: {}, {}",
                    new Object[]{networkName, i, clientId, url});
            i++;
        }
        logger.warn("Joining not implemented.");
    }
}
