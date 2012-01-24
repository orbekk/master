package com.orbekk.paxos;

public interface PaxosService {
    
    /**
     * @return N == proposalNumber if a promise is made.
     *      -M if another promise already was made, where M is the promise
     *      highest proposal number.
     */
    int propose(String clientUrl, int proposalNumber);
    int acceptRequest(String clientUrl, int proposalNumber); 
}
