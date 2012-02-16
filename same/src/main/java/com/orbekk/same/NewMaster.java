package com.orbekk.same;

import java.util.List;
import com.orbekk.util.WorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewMaster {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConnectionManager connections;
    private State state;
    private Broadcaster broadcaster;

    public static NewMaster create(ConnectionManager connections,
            Broadcaster broadcaster, String myUrl, String networkName) {
        State state = new State(networkName);
        state.update(".masterUrl", myUrl, 1);
        return new NewMaster(state, connections, broadcaster);
    }

    NewMaster(State initialState, ConnectionManager connections,
            Broadcaster broadcaster) {
        this.state = initialState;
        this.connections = connections;
        this.broadcaster = broadcaster;
    }

    private MasterService serviceImpl = new MasterService() {
        @Override
        public boolean updateStateRequest(String component,
                String newData, long revision) {
            return false;
        }

        @Override
        public void joinNetworkRequest(String clientUrl) {
        }
    };

    WorkQueue<String> updateStateRequestThread = new WorkQueue<String>() {
        @Override protected void onChange() {
            List<String> pending = getAndClear();
            for (String componentName : pending) {
                logger.info("Component updated: {}", componentName); 
            }
        }
    };

    void performWork() {
        updateStateRequestThread.performWork();
    }

    public void start() {
        updateStateRequestThread.start();
    }

    public void interrupt() {
        updateStateRequestThread.interrupt();
    }

    public MasterService getService() {
        return serviceImpl;
    }
}
