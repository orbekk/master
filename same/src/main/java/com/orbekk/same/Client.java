package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.paxos.MasterProposer;
import com.orbekk.protobuf.Rpc;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.MasterState;
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
    
    private List<StateChangedListener> stateListeners =
            new ArrayList<StateChangedListener>();

    public class ClientInterfaceImpl implements ClientInterface {
        private ClientInterfaceImpl() {
        }

        /** Get a copy of all the client state.
         */
        @Override
        public State getState() {
            return new State(state);
        }

        @Override
        public DelayedOperation set(Component component) {
            final DelayedOperation op = new DelayedOperation();
            if (connectionState != ConnectionState.STABLE) {
                op.complete(DelayedOperation.Status.createError(
                        "Not connected to master: " + connectionState));
                return op;
            }
            
            Services.Master master = connections.getMaster0(
                    masterInfo.getMasterLocation());
            if (master == null) {
                op.complete(DelayedOperation.Status.createError(
                        "Not connected to master."));
                startMasterElection();
                return op;
            }
            final Rpc rpc = new Rpc();
            RpcCallback<Services.UpdateComponentResponse> done =
                    new RpcCallback<Services.UpdateComponentResponse>() {
                @Override
                public void run(Services.UpdateComponentResponse response) {
                    if (!rpc.isOk()) {
                        logger.warn("Master failed to respond to update " +
                        		"request: {}", rpc);
                        op.complete(DelayedOperation.Status.createError(
                                "Error contacting master. Try again later."));
                        startMasterElection();
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
            stateListeners.add(listener);
        }

        @Override
        public void removeStateListener(StateChangedListener listener) {
            stateListeners.remove(listener);
        }

        @Override
        public ConnectionState getConnectionState() {
            return Client.this.getConnectionState();
        }
    }

    private ClientInterface clientInterface = new ClientInterfaceImpl();

    private Services.Client newServiceImpl = new Services.Client() {
        @Override public void setState(RpcController controller,
                Services.Component request, RpcCallback<Empty> done) {
            boolean status = state.update(request.getId(), request.getData(),
                    request.getRevision());
            if (status) {
                for (StateChangedListener listener : stateListeners) {
                    listener.stateChanged(state.getComponent(request.getId()));
                }
            } else {
                logger.warn("Ignoring update: {) => {}",
                        state.getComponent(request.getId()),
                        new State.Component(request.getId(), request.getRevision(),
                                request.getData()));
            }
            done.run(Empty.getDefaultInstance());
        }

        @Override public void masterTakeover(RpcController controller,
                MasterState request, RpcCallback<Empty> done) {
            logger.info("MasterTakeover({})", request);
            if (masterInfo != null &&
                    request.getMasterId() <= masterInfo.getMasterId()) {
                logger.warn("{} tried to take over, but current master is " +
                        "{}:{}. Ignoring", new Object[]{request,
                        state.getDataOf(".masterUrl"),
                        masterInfo.getMasterId()}); 
                return;
            }
            abortMasterElection();
            masterInfo = request;
            connectionState = ConnectionState.STABLE;
            done.run(Empty.getDefaultInstance());
        }

        @Override public void masterDown(RpcController controller, MasterState request,
                RpcCallback<Empty> done) {
            logger.warn("Master down({})", request);
            if (request.getMasterId() < masterInfo.getMasterId()) {
                logger.info("Master {} is down, but current master is {}. Ignoring.",
                        request.getMasterId(), masterInfo.getMasterId());
                return;
            }
            connectionState = ConnectionState.UNSTABLE;
            done.run(Empty.getDefaultInstance());
            tryBecomeMaster(request);
        }
    };
    
    private ClientService serviceImpl = new ClientService() {
        RpcCallback<Empty> noOp = new RpcCallback<Empty>() {
            @Override public void run(Empty unused) {
            }
        };
        
        @Override
        public void setState(String component, String data, long revision) throws Exception {
            logger.info("SetState: {}, {}, {}",
                    new Object[]{component, data, revision});
            Services.Component request = Services.Component.newBuilder()
                    .setId(component)
                    .setData(data)
                    .setRevision(revision)
                    .build();
            newServiceImpl.setState(null, request, noOp);
        }

        @Override
        public synchronized void masterTakeover(String masterUrl, String networkName, 
                int masterId, String masterLocation) throws Exception {
            Services.MasterState request = Services.MasterState.newBuilder()
                    .setMasterUrl(masterUrl)
                    .setNetworkName(networkName)
                    .setMasterId(masterId)
                    .setMasterLocation(masterLocation)
                    .build();
            newServiceImpl.masterTakeover(null, request, noOp);
        }

        @Override
        public void masterDown(int masterId) throws Exception {
            Services.MasterState request = masterInfo.toBuilder()
                    .setMasterId(masterId)
                    .build();
            newServiceImpl.masterDown(null, request, noOp);
        }
    };

    public Client(State state, ConnectionManager connections,
            String myUrl, String myLocation) {
        this.state = state;
        this.connections = connections;
        this.myUrl = myUrl;
        this.myLocation = myLocation;
    }

    public void start() {
    }

    public void interrupt() {
        connectionState = ConnectionState.DISCONNECTED;
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
        connectionState = ConnectionState.UNSTABLE;
        reset();
        
        Services.Master master =
                connections.getMaster0(masterInfo.getMasterLocation());
        final Rpc rpc = new Rpc();
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

    public ClientService getService() {
        return serviceImpl;
    }
    
    public Services.Client getNewService() {
        return newServiceImpl;
    }
    
    private void tryBecomeMaster(MasterState failedMaster) {
        List<String> paxosUrls = state.getList(State.PARTICIPANTS);
        paxosUrls.remove(failedMaster.getMasterLocation());
        MasterProposer proposer = new MasterProposer(getClientState(), paxosUrls,
                connections);
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
        if (!currentMasterProposal.isCancelled() && result != null) {
            masterController.enableMaster(new State(state), result);
        }
    }
    
    private synchronized void abortMasterElection() {
        if (currentMasterProposal != null && !currentMasterProposal.isDone()) {
            boolean status = currentMasterProposal.cancel(true);
            logger.info("Abort status: {}", status);
        }
    }
    
    public void startMasterElection() {
        List<String> participants = state.getList(State.PARTICIPANTS);
        final MasterState failedMaster = masterInfo;
        
        RpcCallback<Empty> done = new RpcCallback<Empty>() {
            @Override public void run(Empty unused) {
                // Ignore unresponsive clients - master election will take
                // care of them.
            }
        };
        
        for (String location : participants) {
            Rpc rpc = new Rpc();
            Services.Client client = connections.getClient0(location);
            if (client != null) {
                client.masterDown(rpc, failedMaster, done);
            }
        }
    }
}
