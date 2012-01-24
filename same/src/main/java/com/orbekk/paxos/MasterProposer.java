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
    
    private boolean internalPropose(int proposalNumber) {
        int promises = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            boolean success = paxos.propose(myUrl, proposalNumber);
            if (success) {
                promises += 1;
            }
        }
        return promises > paxosUrls.size() / 2;
    }
    
    private boolean internalAcceptRequest(int proposalNumber) {
        int accepts = 0;
        for (String url : paxosUrls) {
            PaxosService paxos = connections.getPaxos(url);
            boolean success = paxos.acceptRequest(myUrl, proposalNumber);
            if (success) {
                accepts += 1;
            }
        }
        return accepts > paxosUrls.size() / 2;        
    }

    public boolean propose(int proposalNumber) {
        boolean success = false;
        success = internalPropose(proposalNumber);
        if (success) {
            success = internalAcceptRequest(proposalNumber);
        }
        if (success) {
            return true;
        } else {
            return false;
        }
    }
}
