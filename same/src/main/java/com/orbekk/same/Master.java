package com.orbekk.same;

import com.orbekk.same.State.Component;
import java.util.Collections;
import java.util.List;
import com.orbekk.util.WorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Master {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConnectionManager connections;
    private String myUrl;
    State state;
    private Broadcaster broadcaster;

    public static Master create(ConnectionManager connections,
            Broadcaster broadcaster, String myUrl, String networkName) {
        State state = new State(networkName);
        state.update(".masterUrl", myUrl, 1);
        return new Master(state, connections, broadcaster, myUrl);
    }

    Master(State initialState, ConnectionManager connections,
            Broadcaster broadcaster, String myUrl) {
        this.state = initialState;
        this.connections = connections;
        this.broadcaster = broadcaster;
        this.myUrl = myUrl;
    }

    private MasterService serviceImpl = new MasterService() {
        @Override
        public boolean updateStateRequest(String component,
                String newData, long revision) {
            logger.info("updateStateRequest({}, {}, {})",
                    new Object[]{component, newData, revision});
            boolean updated = state.update(component, newData, revision + 1);
            if (updated) {
                updateStateRequestThread.add(component);
            }
            return updated;
        }

        @Override
        public void joinNetworkRequest(String clientUrl) {
            logger.info("joinNetworkRequest({})", clientUrl);
            List<String> participants = state.getList(".participants");
            sendFullStateThread.add(clientUrl);
            if (!participants.contains(clientUrl)) {
                participants.add(clientUrl);
                synchronized (Master.this) {
                    state.updateFromObject(".participants", participants,
                            state.getRevision(".participants") + 1);
                }
                updateStateRequestThread.add(".participants");
            } else {
                logger.warn("Client {} joining: Already part of network");
            }
        }
    };

    WorkQueue<String> updateStateRequestThread = new WorkQueue<String>() {
        @Override protected void onChange() {
            List<String> pending = getAndClear();
            logger.info("updateStateRequestThread: Updated state: {}",
                    pending);
            for (String componentName : pending) {
                Component component = state.getComponent(componentName);
                List<String> participants = state.getList(".participants");
                broadcastNewComponents(participants,
                        Collections.singletonList(component));
            }
        }
    };

    WorkQueue<String> sendFullStateThread = new WorkQueue<String>() {
        @Override protected void onChange() {
            List<String> pending = getAndClear();
            logger.info("Sending full state to {}", pending);
            final List<Component> components = state.getComponents();
            broadcastNewComponents(pending, components);
        }
    };

    void performWork() {
        sendFullStateThread.performWork();
        updateStateRequestThread.performWork();
    }

    public void start() {
        sendFullStateThread.start();
        updateStateRequestThread.start();
    }

    public void interrupt() {
        sendFullStateThread.interrupt();
        updateStateRequestThread.interrupt();
    }

    public MasterService getService() {
        return serviceImpl;
    }

    private synchronized void removeParticipant(String url) {
        List<String> participants = state.getList(".participants");
        if (participants.contains(url)) {
            logger.info("removeParticipant({})", url);
            participants.remove(url);
            state.updateFromObject(".participants", participants,
                    state.getRevision(".participants") + 1);
            updateStateRequestThread.add(".participants");
        }
    }

    private void broadcastNewComponents(List<String> destinations,
            final List<State.Component> components) {
        broadcaster.broadcast(destinations, new ServiceOperation() {
            @Override public void run(String url) {
                ClientService client = connections.getClient(url);
                try {
                    for (Component c : components) {
                        client.setState(c.getName(), c.getData(),
                                c.getRevision());
                    }
                    client.masterTakeover(
                            state.getDataOf(".masterUrl"),
                            state.getDataOf(".networkName"),
                            0);
                } catch (Exception e) {
                    logger.info("Client {} failed to receive state update.", url);
                    removeParticipant(url);
                }
            }
        });
    }

    /** This master should take over from an earlier master. */
    public void resumeFrom(State lastKnownState) {
        state = lastKnownState;
        broadcaster.broadcast(state.getList(".participants"),
                new ServiceOperation() {
            @Override
            public void run(String url) {
                ClientService client = connections.getClient(url);
                try {
                    client.masterTakeover(myUrl,
                            state.getDataOf(".networkName"), 0);
                } catch (Exception e) {
                    logger.info("Client {} failed to acknowledge new master. " +
                    		"Removing {}", url);
                    removeParticipant(url);
                }
            }
        });
    }
}
