package com.orbekk.paxos;

public interface PaxosService {
    boolean propose(String clientUrl, int proposalNumber);
    boolean acceptRequest(String clientUrl, int proposalNumber); 
}
