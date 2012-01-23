package com.orbekk.paxos;

import java.util.ArrayList;
import java.util.List;

import com.orbekk.same.ConnectionManager;

public class MasterProposer implements Runnable {
    private String myUrl;
    private int roundId = 0;
    private int proposalNumber = 0;
    private List<String> paxosUrls = new ArrayList<String>();
    private Runnable roundFailedAction;
    private Runnable masterAction;
    private ConnectionManager connections;
    
    public static Runnable getTimeoutAction(final long milliseconds) {
        return new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                    // Ignore interrupts.
                }
            }
        };
    }
    
    MasterProposer(String clientUrl, List<String> paxosUrls, int roundId,
            ConnectionManager connections, Runnable roundFailedAction,
            Runnable masterAction) {
        this.myUrl = clientUrl;
        this.paxosUrls = paxosUrls;
        this.roundId = roundId;
        this.connections = connections;
        this.roundFailedAction = roundFailedAction;
        this.masterAction = masterAction;
    }
    
    private boolean propose(int roundId, int proposalNumber) {
        int promises = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            boolean success = paxos.propose(myUrl, roundId, proposalNumber);
            if (success) {
                promises += 1;
            }
        }
        return promises > paxosUrls.size() / 2;
    }
    
    private boolean acceptRequest(int roundId, int proposalNumber) {
        int accepts = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            boolean success = paxos.acceptRequest(myUrl, roundId, proposalNumber);
            if (success) {
                accepts += 1;
            }
        }
        return accepts > paxosUrls.size() / 2;        
    }
    
    @Override public void run() {
        boolean success = false;
        success = propose(roundId + 1, proposalNumber + 1);
        if (success) {
            success = acceptRequest(roundId + 1, proposalNumber + 1);
        }
        if (success) {
            masterAction.run();
        } else {
            roundFailedAction.run();
            // TODO: Next round?
        }
    }
}
