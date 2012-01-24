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
    
    private int internalPropose(int proposalNumber) {
        int bestPromise = proposalNumber;
        int promises = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            int result = paxos.propose(myUrl, proposalNumber);
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
        int bestAccepted = proposalNumber;
        int accepts = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            int result = paxos.acceptRequest(myUrl, proposalNumber);
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

    public boolean propose(int proposalNumber) {
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
}
