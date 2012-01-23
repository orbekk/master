package com.orbekk.paxos;

import java.util.ArrayList;
import java.util.List;

import com.orbekk.same.ConnectionManager;

public class MasterProposer {
    private String myUrl;
    private List<String> paxosUrls = new ArrayList<String>();
    private ConnectionManager connections;
    
    public MasterProposer(String clientUrl, List<String> paxosUrls,
            ConnectionManager connections) {
        this.myUrl = clientUrl;
        this.paxosUrls = paxosUrls;
        this.connections = connections;
    }
    
    private boolean internalPropose(int roundId, int proposalNumber) {
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
    
    private boolean internalAcceptRequest(int roundId, int proposalNumber) {
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

    public boolean propose(int roundId, int proposalNumber) {
        boolean success = false;
        success = internalPropose(roundId, proposalNumber);
        if (success) {
            success = internalAcceptRequest(roundId, proposalNumber);
        }
        if (success) {
            return true;
        } else {
            return false;
        }
    }
}
