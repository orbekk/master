package com.orbekk.paxos;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.Rpc;
import com.orbekk.same.ConnectionManager;
import com.orbekk.same.RpcFactory;
import com.orbekk.same.Services;
import com.orbekk.same.Services.ClientState;
import com.orbekk.same.Services.PaxosRequest;
import com.orbekk.same.Services.PaxosResponse;

public class MasterProposer extends Thread {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ClientState client;
    private final List<String> paxosLocations;
    private final ConnectionManager connections;
    private final RpcFactory rpcf;
    
    public MasterProposer(ClientState client, List<String> paxosLocations,
            ConnectionManager connections, RpcFactory rpcf) {
        this.client = client;
        this.paxosLocations = paxosLocations;
        this.connections = connections;
        this.rpcf = rpcf;
    }
    
    private class ResponseHandler implements RpcCallback<PaxosResponse> {
        final int proposalNumber;
        final int numRequests;
        AtomicInteger bestPromise = new AtomicInteger();
        AtomicInteger numPromises = new AtomicInteger(0);
        AtomicInteger numResponses = new AtomicInteger(0);
        AtomicInteger result = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(1);
        
        public ResponseHandler(int proposalNumber, int numRequests) {
            this.proposalNumber = proposalNumber;
            this.numRequests = numRequests;
            bestPromise.set(-proposalNumber);
        }
        
        @Override public void run(PaxosResponse response) {
            numResponses.incrementAndGet();
            if (response != null) {
                int result = response.getResult();
                if (result == proposalNumber) {
                    numPromises.incrementAndGet();
                }
                while (true) {
                    int oldVal = bestPromise.get();
                    int update = Math.min(oldVal, result);
                    if (bestPromise.compareAndSet(oldVal, update)) {
                        break;
                    }
                }
            }
            checkDone();
        }
        
        private void checkDone() {
            if (done.getCount() > 0) {
                if (numPromises.get() > numRequests / 2) {
                    result.set(proposalNumber);
                    done.countDown();
                } else if (numResponses.get() >= numRequests) {
                    result.set(bestPromise.get());
                    done.countDown();
                }
            }
        }
        
        public int getResult() throws InterruptedException {
            done.await();
            logger.info("ResponseHandler: {} / {} successes.",
                    numPromises.get(), numRequests);
            return result.get();
        }
    }

    private int internalPropose(int proposalNumber)
            throws InterruptedException {
        ResponseHandler handler = new ResponseHandler(proposalNumber,
                paxosLocations.size());
        for (String location : paxosLocations) {
            Rpc rpc = rpcf.create();
            Services.Paxos paxos = connections.getPaxos0(location);
            if (paxos == null) {
                handler.run(null);
                continue;
            }
            PaxosRequest request = PaxosRequest.newBuilder()
                    .setClient(client)
                    .setProposalNumber(proposalNumber)
                    .build();
            paxos.propose(rpc, request, handler);
        }
        return handler.getResult();
    }

    private int internalAcceptRequest(int proposalNumber)
            throws InterruptedException {
        ResponseHandler handler = new ResponseHandler(proposalNumber,
                paxosLocations.size());
        for (String location : paxosLocations) {
            Rpc rpc = rpcf.create();
            Services.Paxos paxos = connections.getPaxos0(location);
            PaxosRequest request = PaxosRequest.newBuilder()
                    .setClient(client)
                    .setProposalNumber(proposalNumber)
                    .build();
            paxos.acceptRequest(rpc, request, handler);
            rpc.await();
            logger.info("Rpc result from paxos.acceptRequest: " + rpc.errorText());
        }
        return handler.getResult();
    }

    boolean propose(int proposalNumber) throws InterruptedException {
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

    boolean proposeRetry(int proposalNumber) throws InterruptedException {
        return proposeRetry(proposalNumber, null) != null;
    }
    
    Integer proposeRetry(int proposalNumber, Runnable retryAction)
            throws InterruptedException {
        logger.info("Paxos services: {}.", paxosLocations);
        assert proposalNumber > 0;
        int nextProposal = proposalNumber;
        int result = nextProposal - 1;

        while (!Thread.currentThread().isInterrupted() && result != nextProposal) {
            result = internalPropose(nextProposal);
            if (result == nextProposal) {
                result = internalAcceptRequest(nextProposal);
            }
            logger.info("Proposed value {}, result {}.",
                    nextProposal, result);
            if (result < 0) {
                nextProposal = -result + 1;
                if (retryAction != null) {
                    retryAction.run();
                }
            }
        }
        if (Thread.interrupted()) {
            throw new InterruptedException();
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
