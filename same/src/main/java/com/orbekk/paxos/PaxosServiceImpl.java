package com.orbekk.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class better be thread-safe.
 */
public class PaxosServiceImpl implements PaxosService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int highestPromise = 0;
    private int highestAcceptedValue = 0;
    private String tag = "";

    public PaxosServiceImpl(String tag) {
        this.tag = tag;
    }

    @Override
    public synchronized int propose(String clientUrl,
            int proposalNumber) {
        if (proposalNumber > highestPromise) {
            logger.info(tag + "propose({}, {}) = accepted",
                    new Object[]{clientUrl, proposalNumber});
            highestPromise = proposalNumber;
            return highestPromise;
        } else {
            logger.info(tag + "propose({}, {}) = rejected " +
                    "(promised: {})",
                    new Object[]{clientUrl, proposalNumber,
                            highestPromise});
            return -highestPromise;
        }
    }

    @Override
    public synchronized int acceptRequest(String clientUrl,
            int proposalNumber) {
        if (proposalNumber == highestPromise) {
            logger.info(tag + "acceptRequest({}, {}) = accepted",
                    new Object[]{clientUrl, proposalNumber});
            highestAcceptedValue = proposalNumber;
            return highestAcceptedValue;
        } else {
            logger.info(tag + "acceptRequest({}, {}) = rejected " +
                    "(promise={})",
                    new Object[]{clientUrl, proposalNumber,
                            highestPromise});
            return -highestPromise;
        }
    }
}
