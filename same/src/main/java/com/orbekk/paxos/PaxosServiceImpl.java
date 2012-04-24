package com.orbekk.paxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.same.Services.Paxos;
import com.orbekk.same.Services.PaxosRequest;
import com.orbekk.same.Services.PaxosResponse;

/**
 * This class better be thread-safe.
 */
public class PaxosServiceImpl implements PaxosService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int highestPromise = 0;
    private int highestAcceptedValue = 0;
    private String tag = "";
    private Paxos service = new ProtobufPaxosServiceImpl();
    
    private class ProtobufPaxosServiceImpl extends Paxos {
        @Override
        public void propose(RpcController controller, PaxosRequest request,
                RpcCallback<PaxosResponse> done) {
            String clientUrl = request.getClient().getLocation();
            int proposalNumber = request.getProposalNumber();
            int response = 
                    PaxosServiceImpl.this.propose(clientUrl, proposalNumber);
            PaxosResponse result = PaxosResponse.newBuilder()
                    .setResult(response)
                    .build();
            done.run(result);
        }

        @Override
        public void acceptRequest(RpcController controller,
                PaxosRequest request, RpcCallback<PaxosResponse> done) {
            String clientUrl = request.getClient().getLocation();
            int proposalNumber = request.getProposalNumber();
            int response = 
                    PaxosServiceImpl.this.acceptRequest(clientUrl, proposalNumber);
            PaxosResponse result = PaxosResponse.newBuilder()
                    .setResult(response)
                    .build();
            done.run(result);
        }
        
    }
    
    public PaxosServiceImpl(String tag) {
        this.tag = tag;
    }
    
    public Paxos getService() {
        return service;
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
