package com.orbekk.same;

import static com.orbekk.same.StackTraceUtil.throwableToString;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.util.WorkQueue;

public class ClientServiceImpl implements UrlReceiver,
            DiscoveryListener {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConnectionManager connections;
    private State state;
    private String myUrl = null;
    private StateChangedListener stateListener;
    private NetworkNotificationListener networkListener;
    
    private ClientService serviceImpl = new ClientService() {
        @Override
        public void setState(String component, String data, long revision) throws Exception {
            boolean status = state.update(component, data, revision);
            if (status) {
                if (stateListener != null) {
                    stateListener.stateChanged(component, data);
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
    };
    
    private WorkQueue<String> discoveryThread = new WorkQueue<String>() {
        @Override protected void onChange() {
            List<String> pending = getAndClear();
            for (String url : pending) {
                discover(url);
            }
        }
    };
    
    public ClientServiceImpl(State state, ConnectionManager connections) {
        this.state = state;
        this.connections = connections;
    }

    public void start() {
        discoveryThread.start();
    }
    
    public void interrupt() {
        discoveryThread.interrupt();
    }

    @Override
    public void setUrl(String myUrl) {
        this.myUrl = myUrl + "ClientService.json";
        logger.info("My URL is {}.", this.myUrl);
    }
    
    public String getUrl() {
        return myUrl;
    }
    
    public void joinNetwork(String masterUrl) {
        if (myUrl != null) {
            MasterService master = connections.getMaster(masterUrl);
            state.clear();
            try {
                master.joinNetworkRequest(myUrl);
            } catch (Exception e) {
                logger.error("Unable to connect to master.", e);
            }          
        } else {
            logger.error("Tried to join network at {}, but my url is unknown. " +
                    "Run discovery service.", masterUrl);
        }
    }
   
    String lib_get(String name) {
        return state.getDataOf(name);
    }
    
    <T> T lib_get(String name, TypeReference<T> type) {
        return state.getParsedData(name, type);
    }
    
    void lib_set(String name, String data) throws UpdateConflict {
        String masterUrl = state.getDataOf(".masterUrl");
        long revision = state.getRevision(name) + 1;
        MasterService master = connections.getMaster(masterUrl);
        try {
            boolean success = master.updateStateRequest(name, data,
                    revision);
            if (!success) {
                throw new UpdateConflict("State update conflict when " +
                        "updating " + name);
            }
        } catch (Exception e) {
            logger.error("Unable to contact master. Update fails.", e);
            throw new UpdateConflict("Unable to contact master. Update fails.");
        }
    }
    
    public State.Component getState(String name) {
        return state.getComponent(name);
    }
    
    State testGetState() {
        return state;
    }

    public void setStateChangedListener(StateChangedListener listener) {
        this.stateListener = listener;
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
        String clientUrl = url + "ClientService.json";
        if (!url.equals(myUrl)) {
            try {
                connections.getClient(clientUrl)
                        .notifyNetwork(state.getDataOf(".networkName"),
                                state.getDataOf(".masterUrl"));
            } catch (Exception e) {
                logger.warn("Failed to contact new client {}: {}", clientUrl,
                        throwableToString(e));
            }
        }
    }
    
    public ClientService getService() {
        return serviceImpl;
    }
}
