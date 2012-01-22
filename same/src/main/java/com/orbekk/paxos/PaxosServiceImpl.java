package com.orbekk.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class better be thread-safe.
 */
public class PaxosServiceImpl implements PaxosService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int roundId = 0;
    private int highestPromise = 0;
    private String tag = "";

    public PaxosServiceImpl(String tag) {
        this.tag = tag;
    }

    @Override
    public synchronized boolean propose(String clientUrl, int roundId,
            int proposalNumber) {
        if (roundId > this.roundId) {
            newRound(roundId);
        }
        if (roundId < this.roundId) {
            logger.info(tag + "propose({}, {}, {}) = rejected " +
                    "(current round: {})",
                    new Object[]{clientUrl, roundId, proposalNumber,
                            this.roundId});
            return false;
        }

        if (proposalNumber > highestPromise) {
            highestPromise = proposalNumber;
            logger.info(tag + "propose({}, {}, {}) = accepted",
                    new Object[]{clientUrl, roundId, proposalNumber});
            return true;
        } else {
            logger.info(tag + "propose({}, {}, {}) = rejected " +
                    "(promised: {})",
                    new Object[]{clientUrl, roundId, proposalNumber,
                            highestPromise});
            return false;
        }
    }

    @Override
    public synchronized boolean acceptRequest(String clientUrl, int roundId,
            int proposalNumber) {
        if (roundId == this.roundId && proposalNumber == highestPromise) {
            logger.info(tag + "acceptRequest({}, {}, {}) = accepted",
                    new Object[]{clientUrl, roundId, proposalNumber});
            finishRound();
            return true;
        } else {
            logger.info(tag + "acceptRequest({}, {}, {}) = rejected " +
                    "(roundId={}, promise={})",
                    new Object[]{clientUrl, roundId, proposalNumber,
                            this.roundId, highestPromise});
            return false;
        }
    }

    private synchronized void finishRound() {
        newRound(roundId + 1);
    }

    private synchronized void newRound(int roundId) {
        logger.info(tag + "new round: {}", roundId);
        this.roundId = roundId;
        highestPromise = 0;
    }
}
