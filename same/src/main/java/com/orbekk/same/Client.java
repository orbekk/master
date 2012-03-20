package com.orbekk.same;

import static com.orbekk.same.StackTraceUtil.throwableToString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.paxos.MasterProposer;
import com.orbekk.same.State.Component;
import com.orbekk.same.discovery.DiscoveryListener;
import com.orbekk.util.DelayedOperation;
import com.orbekk.util.WorkQueue;

public class Client implements DiscoveryListener {
    public static long MASTER_TAKEOVER_TIMEOUT = 4000l;
    private Logger logger = LoggerFactory.getLogger(getClass());
    /** TODO: Not really useful yet. Remove? */
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private ConnectionManager connections;
    State state;
    private String myUrl;
    String masterUrl;
    private int masterId = -1;
    private MasterController masterController = null;
    private Broadcaster broadcaster;
    private Future<Integer> currentMasterProposal = null;
    
    private List<StateChangedListener> stateListeners =
            new ArrayList<StateChangedListener>();
    private NetworkNotificationListener networkListener;

    public class ClientInterfaceImpl implements ClientInterface {
        private ClientInterfaceImpl() {
        }

        /** Get a copy of all the client state.
         */
        @Override
        public State getState() {
            return new State(state);
        }

        // TODO: Do this asynchronously? Currently this is already achieved
        // on Android, which makes the Java and Android versions different.
        @Override
        public DelayedOperation set(Component component) {
            DelayedOperation op = new DelayedOperation();
            if (connectionState != ConnectionState.STABLE) {
                op.complete(DelayedOperation.Status.createError(
                        "Not connected to master: " + connectionState));
                return op;
            }
            MasterService master = connections.getMaster(masterUrl);
            try {
                boolean success = master.updateStateRequest(
                        component.getName(), component.getData(),
                        component.getRevision());
                if (success) {
                    op.complete(DelayedOperation.Status.createOk());
                } else {
                    op.complete(DelayedOperation.Status
                            .createConflict("Conflict from master"));
                }
            } catch (Exception e) {
                logger.error("Unable to contact master. Update fails.", e);
                String e_ = throwableToString(e);
                op.complete(DelayedOperation.Status.createError(
                        "Error contacting master. Update fails: " + e_));
            }
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

    private ClientService serviceImpl = new ClientService() {
        @Override
        public void setState(String component, String data, long revision) throws Exception {
            boolean status = state.update(component, data, revision);
            if (status) {
                for (StateChangedListener listener : stateListeners) {
                    listener.stateChanged(state.getComponent(component));
                }
            } else {
                logger.warn("Ignoring update: {}",
                        new State.Component(component, revision, data));
            }            
        }

        @Override
        public void notifyNetwork(String networkName, String masterUrl) throws Exception {
            logger.info("NotifyNetwork(networkName={}, masterUrl={})", 
                    networkName, masterUrl);
            if (networkListener != null) {
                networkListener.notifyNetwork(networkName, masterUrl);
            }            
        }

        @Override
        public void discoveryRequest(String remoteUrl) {
            discoveryThread.add(remoteUrl);
        }

        @Override
        public void masterTakeover(String masterUrl, String networkName, 
                int masterId) throws Exception {
            logger.info("MasterTakeover({}, {}, {})",
                    new Object[]{masterUrl, networkName, masterId});
            abortMasterElection();
            Client.this.masterUrl = masterUrl;
            connectionState = ConnectionState.STABLE;
        }

        @Override
        public void masterDown() throws Exception {
            logger.info("Master is down.");
            connectionState = ConnectionState.UNSTABLE;
            tryBecomeMaster();
        }
    };

    private WorkQueue<String> discoveryThread = new WorkQueue<String>() {
        @Override protected void onChange() {
            List<String> pending = getAndClear();
            for (String url : pending) {
                discover(url);
            }
        }
    };

    public Client(State state, ConnectionManager connections,
            String myUrl, Broadcaster broadcaster) {
        this.state = state;
        this.connections = connections;
        this.myUrl = myUrl;
        this.broadcaster = broadcaster;
    }

    public void start() {
        discoveryThread.start();
    }

    public void interrupt() {
        connectionState = ConnectionState.DISCONNECTED;
        discoveryThread.interrupt();
    }

    void performWork() {
        discoveryThread.performWork();
    }
    
    public String getUrl() {
        return myUrl;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }
    
    public void setMasterController(MasterController masterController) {
        this.masterController = masterController;
    }
    
    public void joinNetwork(String masterUrl) {
        logger.info("joinNetwork({})", masterUrl);
        connectionState = ConnectionState.UNSTABLE;
        MasterService master = connections.getMaster(masterUrl);
        state.clear();
        try {
            master.joinNetworkRequest(myUrl);
        } catch (Exception e) {
            logger.error("Unable to connect to master.", e);
        }          
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

    public void setNetworkListener(NetworkNotificationListener listener) {
        this.networkListener = listener;
    }

    public void sendDiscoveryRequest(String url) {
        try {
            connections.getClient(url)
            .discoveryRequest(myUrl);
        } catch (Exception e) {
            logger.warn("Failed to send discovery request: {}",
                    throwableToString(e));
        }
    }

    @Override
    public void discover(String url) {
        String networkName = state.getDataOf(".networkName");
        if (networkName.equals(".InvalidClientNetwork")) {
            logger.warn("Client not joined to a network. Ignoring discovery");
            return;
        } else if (networkName.equals(".Private")) {
            logger.info("Ignoring broadcast to .Private network.");
            return;
        }

        if (!url.equals(myUrl)) {
            try {
                connections.getClient(url)
                .notifyNetwork(state.getDataOf(".networkName"),
                        state.getDataOf(".masterUrl"));
            } catch (Exception e) {
                logger.warn("Failed to contact new client {}: {}", url,
                        throwableToString(e));
            }
        }
    }

    public ClientService getService() {
        return serviceImpl;
    }
    
    private List<String> getPaxosUrls() {
        List<String> paxosUrls = new ArrayList<String>();
        for (String participant : state.getList(".participants")) {
            paxosUrls.add(participant.replace("ClientService", "PaxosService"));
        }
        return paxosUrls;
    }
    
    private void tryBecomeMaster() {
        List<String> paxosUrls = getPaxosUrls();
        MasterProposer proposer = new MasterProposer(getUrl(), paxosUrls,
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
            currentMasterProposal = proposer.startProposalTask(1, sleeperTask);
        }
        Integer result = null;
        try {
            result = currentMasterProposal.get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (CancellationException e) {
        }
        if (!currentMasterProposal.isCancelled() && result != null) {
            masterController.enableMaster(state);
        }
    }
    
    private synchronized void abortMasterElection() {
        if (currentMasterProposal != null && !currentMasterProposal.isDone()) {
            boolean status = currentMasterProposal.cancel(true);
            logger.info("Abort status: {}", status);
        }
    }
    
    public void startMasterElection() {
        List<String> participants = state.getList(".participants");
        broadcaster.broadcast(participants, new ServiceOperation() {
            @Override public void run(String url) {
                ClientService client = connections.getClient(url);
                try {
                    client.masterDown();
                } catch (Exception e) {
                    logger.info("{}.masterDown() did not respond (ignored): " +
                    		url, e);
                }
            }
        });
    }
}
