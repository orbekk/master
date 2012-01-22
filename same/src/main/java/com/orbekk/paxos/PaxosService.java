pakage com.orbekk.paxos;

public interface PaxosService {
    boolean propose(String clientUrl, int roundId, int proposalNumber);
    boolean acceptRequest(String clientUrl, int roundId, int proposalNumber); 
}
