/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class PaxosServiceImpl {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int highestPromise = 0;
    private int highestAcceptedValue = 0;
    private String tag = "";
    private Paxos service = new ProtobufPaxosServiceImpl();
    
    private class ProtobufPaxosServiceImpl extends Paxos {
        @Override
        public void propose(RpcController controller, PaxosRequest request,
                RpcCallback<PaxosResponse> done) {
            logger.info("propose({}). Highest promise: {}, Highest accepted: {}",
                    new Object[]{request, highestPromise, highestAcceptedValue});
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
            logger.info("acceptRequest({}). Highest promise: {}, Highest accepted: {}",
                    new Object[]{request, highestPromise, highestAcceptedValue});
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

    private synchronized int propose(String clientUrl,
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

    private synchronized int acceptRequest(String clientUrl,
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
