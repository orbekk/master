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
package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.paxos.MasterProposer;
import com.orbekk.protobuf.Rpc;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.FullStateResponse;
import com.orbekk.same.Services.MasterState;
import com.orbekk.same.Services.MasterTakeoverResponse;
import com.orbekk.same.State.Component;
import com.orbekk.util.DelayedOperation;

public class Client {
    public static long MASTER_TAKEOVER_TIMEOUT = 4000l;
    private Logger logger = LoggerFactory.getLogger(getClass());
    /** TODO: Not really useful yet. Remove? */
    private volatile ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private final ConnectionManager connections;
    volatile State state;
    private volatile String myUrl;
    private volatile String myLocation;
    private volatile MasterController masterController = null;
    private volatile Future<Integer> currentMasterProposal = null;
    private volatile MasterState masterInfo;
    private final RpcFactory rpcf;
    private final ExecutorService executor;
    private final ClientInterface clientInterface = new ClientInterfaceImpl();
    private final AtomicLong revision = new AtomicLong(0);
    
    private List<StateChangedListener> updateListeners =
            new CopyOnWriteArrayList<StateChangedListener>();

    private List<ConnectionStateListener> connectionStateListeners =
            new CopyOnWriteArrayList<ConnectionStateListener>();
    
    public class ClientInterfaceImpl implements ClientInterface {
        private ClientInterfaceImpl() {
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public DelayedOperation set(Component component) {
            // Callbacks need to report the correct master.
            final MasterState currentMasterInfo = masterInfo;
            final DelayedOperation op = new DelayedOperation();
            if (connectionState != ConnectionState.STABLE) {
                logger.warn("Connection is {}. Delaying update.", connectionState);
                try {
                    awaitConnectionState(ConnectionState.STABLE);
                } catch (InterruptedException e) {
                }
            }
            
            Services.Master master = connections.getMaster0(
                    masterInfo.getMasterLocation());
            if (master == null) {
                op.complete(DelayedOperation.Status.createError(
                        "Not connected to master."));
                startMasterElection(currentMasterInfo);
                return op;
            }
            final Rpc rpc = rpcf.create();
            RpcCallback<Services.UpdateComponentResponse> done =
                    new RpcCallback<Services.UpdateComponentResponse>() {
                @Override
                public void run(Services.UpdateComponentResponse response) {
                    if (!rpc.isOk()) {
                        logger.warn("Master failed to respond to update " +
                        		"request: {}", rpc.errorText());
                        op.complete(DelayedOperation.Status.createError(
                                "Error contacting master. Try again later."));
                        startMasterElection(currentMasterInfo);
                    } else {
                        if (response.getSuccess()) {
                            op.complete(DelayedOperation.Status.createOk());
                        } else {
                            op.complete(DelayedOperation.Status.createConflict(
                                    "Conflicting update."));
                        }
                    }
                }
            };
            Services.Component request = Services.Component.newBuilder()
                    .setId(component.getName())
                    .setData(component.getData())
                    .setRevision(component.getRevision())
                    .build();
            master.updateStateRequest(rpc, request, done);
            return op;
        }

        @Override
        public void addStateListener(StateChangedListener listener) {
            updateListeners.add(listener);
        }

        @Override
        public void removeStateListener(StateChangedListener listener) {
            updateListeners.remove(listener);
        }

        @Override
        public ConnectionState getConnectionState() {
            return Client.this.getConnectionState();
        }
        
        @Override public void addConnectionStateListener(
                ConnectionStateListener listener) {
            connectionStateListeners.add(listener);
        }
        
        @Override public void removeConnectionStateListener(
                ConnectionStateListener listener) {
            connectionStateListeners.remove(listener);
        }
    }

    private Services.Client newServiceImpl = new Services.Client() {
        @Override public void setState(RpcController controller,
                Services.Component request, RpcCallback<Empty> done) {
            boolean status = state.update(request.getId(), request.getData(),
                    request.getRevision());
            if (status) {
                for (StateChangedListener listener : updateListeners) {
                    listener.stateChanged(state.getComponent(request.getId()));
                }
                updateRevision(request.getRevision());
            } else {
                logger.warn("Ignoring update: {) => {}",
                        state.getComponent(request.getId()),
                        new State.Component(request.getId(), request.getRevision(),
                                request.getData()));
            }
            done.run(Empty.getDefaultInstance());
        }

        @Override public void masterTakeover(RpcController controller,
                MasterState request, RpcCallback<MasterTakeoverResponse> done) {
            logger.info("MasterTakeover({})", request);
            if (masterInfo != null &&
                    request.getMasterId() <= masterInfo.getMasterId()) {
                logger.warn("{} tried to take over, but current master is " +
                        "{}. Ignoring", masterInfo); 
                return;
            }
            abortMasterElection();
            long highestRevision = 0;
            if (masterInfo != null && request.getNetworkName().equals(masterInfo.getNetworkName())) {
                highestRevision = revision.get();
            }
            masterInfo = request;
            setConnectionState(ConnectionState.STABLE);
            done.run(MasterTakeoverResponse.newBuilder()
                    .setHighestKnownRevision(highestRevision)
                    .build());
        }

        @Override public void masterDown(RpcController controller, MasterState request,
                RpcCallback<Empty> done) {
            logger.warn("Master down({})", request);
            if (request.getMasterId() < masterInfo.getMasterId()) {
                logger.info("Master {} is down, but current master is {}. Ignoring.",
                        request.getMasterId(), masterInfo.getMasterId());
                return;
            }
            setConnectionState(ConnectionState.UNSTABLE);
            executor.execute(new MasterStarter(request));
            done.run(Empty.getDefaultInstance());
        }

        @Override
        public void getFullState(RpcController controller, Empty request,
                RpcCallback<FullStateResponse> done) {
            FullStateResponse.Builder response = FullStateResponse.newBuilder();
            response.setRevision(revision.get());
            response.addAllComponent(
                    ServicesPbConversion.componentsToPb(state.getComponents()));
            done.run(response.build());
        }
    };
    
    private class MasterStarter implements Runnable {
        private final MasterState failedMaster;
        
        public MasterStarter(MasterState failedMaster) {
            this.failedMaster = failedMaster;
        }
        
        @Override public void run() {
            logger.info("Trying to become master. Failed master: {}.",
                    failedMaster);
            List<String> paxosUrls = state.getList(State.PARTICIPANTS);
            paxosUrls.remove(failedMaster.getMasterLocation());
            MasterProposer proposer = new MasterProposer(getClientState(), paxosUrls,
                    connections, rpcf);
            if (masterController == null) {
                logger.warn("Could not become master: No master controller.");
                return;
            }
            Runnable sleeperTask = new Runnable() {
                @Override public synchronized void run() {
                    try {
                        wait(MASTER_TAKEOVER_TIMEOUT);
                    } catch (InterruptedException e) {
                    }
                }
            };
            synchronized (this) {
                if (failedMaster.getMasterId() < masterInfo.getMasterId()) {
                    logger.info("Master election aborted. Master already chosen.");
                    return;
                }
                currentMasterProposal = proposer.startProposalTask(
                        masterInfo.getMasterId() + 1, sleeperTask);
            }
            Integer result = null;
            try {
                result = currentMasterProposal.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                logger.error("Error electing master: ", e);
            } catch (CancellationException e) {
            }
            if (!currentMasterProposal.isCancelled() && result != null &&
                    masterInfo.getMasterId() <= failedMaster.getMasterId()) {
                masterController.enableMaster(failedMaster.getNetworkName(),
                        new State(state), result);
            } else {
                logger.info("Master election aborted. Master already chosen.");
            }
        }
    }
    
    public Client(State state, ConnectionManager connections,
            String myUrl, String myLocation, RpcFactory rpcf,
            ExecutorService executor) {
        this.state = state;
        this.connections = connections;
        this.myUrl = myUrl;
        this.myLocation = myLocation;
        this.rpcf = rpcf;
        this.executor = executor;
    }
    
    public void start() {
    }

    public void interrupt() {
        setConnectionState(ConnectionState.DISCONNECTED);
        executor.shutdown();
    }

    void performWork() {
    }
    
    public String getUrl() {
        return myUrl;
    }

    public Services.ClientState getClientState() {
        return Services.ClientState.newBuilder()
                .setUrl(myUrl)
                .setLocation(myLocation)
                .setRevision(revision.get())
                .build();
    }
    
    public MasterState getMaster() {
        return masterInfo;
    }
    
    public ConnectionState getConnectionState() {
        return connectionState;
    }
    
    public void setMasterController(MasterController masterController) {
        this.masterController = masterController;
    }
    
    private synchronized void reset() {
        state.clear();
        masterInfo = null;
    }
    
    public Rpc joinNetwork(Services.MasterState masterInfo) {
        logger.info("joinNetwork({})", masterInfo);
        setConnectionState(ConnectionState.UNSTABLE);
        reset();
        
        Services.Master master =
                connections.getMaster0(masterInfo.getMasterLocation());
        final Rpc rpc = rpcf.create();
        RpcCallback<Empty> done = new RpcCallback<Empty>() {
            @Override public void run(Empty unused) {
                if (!rpc.isOk()) {
                    logger.warn("Failed to join network.");
                }
            }
        };
        master.joinNetworkRequest(rpc, getClientState(), done);
        return rpc;
    }

    public ClientInterface getInterface() {
        return clientInterface;
    }

    public State.Component getState(String name) {
        return state.getComponent(name);
    }

    State testGetState() {
        return state;
    }

    public Services.Client getNewService() {
        return newServiceImpl;
    }
    
    private synchronized void abortMasterElection() {
        if (currentMasterProposal != null && !currentMasterProposal.isDone()) {
            boolean status = currentMasterProposal.cancel(true);
            logger.info("Abort status: {}", status);
        }
    }
    
    public void startMasterElection(MasterState failedMaster) {
        List<String> participants = state.getList(State.PARTICIPANTS);
        
        RpcCallback<Empty> done = new RpcCallback<Empty>() {
            @Override public void run(Empty unused) {
                // Ignore unresponsive clients - master election will take
                // care of them.
            }
        };
        
        for (String location : participants) {
            Rpc rpc = rpcf.create();
            Services.Client client = connections.getClient0(location);
            if (client != null) {
                client.masterDown(rpc, failedMaster, done);
            }
        }
    }
    
    public void updateRevision(long newRevision) {
        boolean updated = false;
        while (!updated) {
            long expected = revision.get();
            long update = Math.max(expected, newRevision);
            updated = revision.compareAndSet(expected, update);
        }
    }
    
    private void setConnectionState(ConnectionState newState) {
        connectionState = newState;
        for (ConnectionStateListener listener : connectionStateListeners) {
            listener.connectionStatusChanged(newState);
        }
    }
    
    private void awaitConnectionState(ConnectionState expected) throws InterruptedException {
        class Listener implements ConnectionStateListener {
            CountDownLatch done = new CountDownLatch(1);
            ConnectionState expected;
            
            public Listener(ConnectionState expected) {
                this.expected = expected;
            }
            
            @Override public void connectionStatusChanged(ConnectionState state) {
                if (state.equals(expected)) {
                    done.countDown();
                }
            }
            
            public void await() throws InterruptedException {
                if (Client.this.connectionState.equals(expected)) {
                    done.countDown();
                }
                done.await();
            }
        }
        Listener listener = new Listener(expected);
        getInterface().addConnectionStateListener(listener);
        listener.await();
    }
}
