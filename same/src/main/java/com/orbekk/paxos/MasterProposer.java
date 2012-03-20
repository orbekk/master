package com.orbekk.paxos;

import static com.orbekk.same.StackTraceUtil.throwableToString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.ConnectionManager;

public class MasterProposer extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String myUrl;
    private List<String> paxosUrls = new ArrayList<String>();
    private ConnectionManager connections;
    
    public MasterProposer(String clientUrl, List<String> paxosUrls,
            ConnectionManager connections) {
        this.myUrl = clientUrl;
        this.paxosUrls = paxosUrls;
        this.connections = connections;
    }

    private int internalPropose(int proposalNumber) {
        int bestPromise = -proposalNumber;
        int promises = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            int result = 0;
            try {
                result = paxos.propose(myUrl, proposalNumber);
            } catch (Exception e) {
                logger.warn("Exception from {}: {}", url,
                        throwableToString(e));
            }
            if (result == proposalNumber) {
                promises += 1;
            }
            bestPromise = Math.min(bestPromise, result);
        }
        if (promises > paxosUrls.size() / 2) {
            return proposalNumber;
        } else {
            return bestPromise;
        }
    }

    private int internalAcceptRequest(int proposalNumber) {
        int bestAccepted = -proposalNumber;
        int accepts = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            int result = 0;
            try {
                result = paxos.acceptRequest(myUrl, proposalNumber);
            } catch (Exception e) {
                logger.warn("Exception from {}: {}", url,
                        throwableToString(e));
            }
            if (result == proposalNumber) {
                accepts += 1;
            }
            bestAccepted = Math.min(bestAccepted, result);
        }
        if (accepts > paxosUrls.size() / 2) {
            return proposalNumber;
        } else {
            return bestAccepted;
        }
    }

    boolean propose(int proposalNumber) {
        int result = internalPropose(proposalNumber);
        if (result == proposalNumber) {
            result = internalAcceptRequest(proposalNumber);
        }
        if (result == proposalNumber) {
            return true;
        } else {
            return false;
        }
    }

    boolean proposeRetry(int proposalNumber) {
        return proposeRetry(proposalNumber, null) != null;
    }
    
    Integer proposeRetry(int proposalNumber, Runnable retryAction) {
        assert proposalNumber > 0;
        int nextProposal = proposalNumber;
        int result = nextProposal - 1;

        while (!Thread.interrupted() && result != nextProposal) {
            result = internalPropose(nextProposal);
            if (result == nextProposal) {
                result = internalAcceptRequest(nextProposal);
            }
            logger.info("Proposed value {}, result {}", nextProposal, result);
            if (result < 0) {
                nextProposal = -result + 1;
                if (retryAction != null) {
                    retryAction.run();
                }
            }
        }
        if (Thread.interrupted()) {
            return null;
        }

        return result;
    }
    
    public Future<Integer> startProposalTask(final int proposalNumber,
            final Runnable retryAction) {
        Callable<Integer> proposalCallable = new Callable<Integer>() {
            @Override public Integer call() throws Exception {
                return proposeRetry(proposalNumber, retryAction);
            }
        };
        FutureTask<Integer> task = new FutureTask<Integer>(proposalCallable);
        new Thread(task).start();
        return task;
    }
}
