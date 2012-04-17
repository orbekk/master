package com.orbekk.same;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.same.Services.ClientState;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.UpdateComponentResponse;
import com.orbekk.same.State.Component;
import com.orbekk.util.WorkQueue;

public class Master {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConnectionManager connections;
    private String myUrl;
    private String myLocation; // Protobuf server location, i.e., myIp:port
    State state;
    private Broadcaster broadcaster;
    private volatile int masterId = 1;
    
    public static Master create(ConnectionManager connections,
            Broadcaster broadcaster, String myUrl, String networkName,
            String myLocation) {
        State state = new State(networkName);
        state.update(".masterUrl", myUrl, 1);
        state.update(".masterLocation", myLocation, 1);
        return new Master(state, connections, broadcaster, myUrl, myLocation);
    }

    Master(State initialState, ConnectionManager connections,
            Broadcaster broadcaster, String myUrl, String myLocation) {
        this.state = initialState;
        this.connections = connections;
        this.broadcaster = broadcaster;
        this.myUrl = myUrl;
        this.myLocation = myLocation;
    }
    
    public String getNetworkName() {
        return state.getDataOf(".networkName");
    }
    
    public String getLocation() {
        return myLocation;
    }

    public String getUrl() {
        return myUrl;
    }
    
    public Services.MasterState getMasterInfo() {
        return Services.MasterState.newBuilder()
                .setMasterUrl(getUrl())
                .setMasterLocation(getLocation())
                .setNetworkName(getNetworkName())
                .setMasterId(masterId)
                .build();
    }
    
    private Services.Master newMasterImpl = new Services.Master() {
        @Override public void joinNetworkRequest(RpcController controller,
                ClientState request, RpcCallback<Empty> done) {
            logger.info("joinNetworkRequest({})", request);
            
            /** Old participant code. */
            List<String> participants = state.getList(".participants");
            sendFullStateThread.add(request.getUrl());
            if (!participants.contains(request.getUrl())) {
                participants.add(request.getUrl());
                synchronized (Master.this) {
                    state.updateFromObject(".participants", participants,
                            state.getRevision(".participants") + 1);
                }
                updateStateRequestThread.add(".participants");
            } else {
                logger.warn("Client {} joining: Already part of network");
            }
            
            /** New participant code. */
            addParticipant(request);
            
            done.run(Empty.getDefaultInstance());
        }


        @Override public void updateStateRequest(RpcController controller,
                Services.Component request,
                RpcCallback<Services.UpdateComponentResponse> done) {
            logger.info("updateStateRequest({})", request);
            boolean updated = state.update(request.getId(), request.getData(),
                    request.getRevision() + 1);
            if (updated) {
                updateStateRequestThread.add(request.getId());
            }
            done.run(Services.UpdateComponentResponse.newBuilder()
                    .setSuccess(updated).build());
        }
    };
    
    private MasterService serviceImpl = new MasterService() {
        @Override
        public boolean updateStateRequest(String component,
                String newData, long revision) {
            Services.Component request = Services.Component.newBuilder()
                    .setId(component)
                    .setData(newData)
                    .setRevision(revision)
                    .build();
            final AtomicBoolean result = new AtomicBoolean(false);
            RpcCallback<Services.UpdateComponentResponse> done =
                    new RpcCallback<Services.UpdateComponentResponse>() {
                @Override public void run(UpdateComponentResponse response) {
                    result.set(response.getSuccess());
                }
            };
            newMasterImpl.updateStateRequest(null, request, done);
            return result.get();
        }

        @Override
        public void joinNetworkRequest(String clientUrl) {
            Services.ClientState request = Services.ClientState.newBuilder()
                    .setUrl(clientUrl)
                    .build();
            RpcCallback<Services.Empty> done =
                    new RpcCallback<Services.Empty>() {
                @Override public void run(Empty response) {
                }
            };
            newMasterImpl.joinNetworkRequest(null, request, done);
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
            broadcaster.broadcast(pending, new ServiceOperation() {
                @Override public void run(String url) {
                    ClientService client = connections.getClient(url);
                    try {
                        client.masterTakeover(
                                state.getDataOf(".masterUrl"),
                                state.getDataOf(".networkName"),
                                masterId,
                                state.getDataOf(".masterLocation"));
                    } catch (Exception e) {
                        logger.info("Client failed to acknowledge master. Remove.",
                                e);
                        removeParticipant(url);
                    }
                }
            });
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

    public Services.Master getNewService() {
        return newMasterImpl;
    }
    
    public MasterService getService() {
        return serviceImpl;
    }

    private synchronized void addParticipant(ClientState client) {
        List<String> participants = state.getList(State.PARTICIPANTS);
        if (!participants.contains(client.getUrl())) {
            participants.add(client.getUrl());
            state.updateFromObject(State.PARTICIPANTS, participants,
                    state.getRevision(State.PARTICIPANTS) + 1);
            updateStateRequestThread.add(State.PARTICIPANTS);
        }
        
    }

    private synchronized void removeParticipant(String url) {
        /** TODO: Remove this code. */
        List<String> participants = state.getList(".participants");
        if (participants.contains(url)) {
            logger.info("removeParticipant({})", url);
            participants.remove(url);
            state.updateFromObject(".participants", participants,
                    state.getRevision(".participants") + 1);
            updateStateRequestThread.add(".participants");
        }
        
        List<String> participants0 = state.getList(State.PARTICIPANTS);
        if (participants0.contains(url)) {
            logger.info("removeParticipant({})", url);
            participants0.remove(url);
            state.updateFromObject(State.PARTICIPANTS, participants0, 
                    state.getRevision(State.PARTICIPANTS) + 1);
            updateStateRequestThread.add(State.PARTICIPANTS);
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
                } catch (Exception e) {
                    logger.info("Client {} failed to receive state update.", url);
                    removeParticipant(url);
                }
            }
        });
    }

    /** This master should take over from an earlier master. */
    public void resumeFrom(State lastKnownState, final int masterId) {
        state = lastKnownState;
        state.update(".masterUrl", myUrl, state.getRevision(".masterUrl") + 100);
        state.update(".masterLocation", myLocation,
                state.getRevision(".masterLocation") + 100);
        this.masterId = masterId;
        broadcaster.broadcast(state.getList(".participants"),
                new ServiceOperation() {
            @Override
            public void run(String url) {
                ClientService client = connections.getClient(url);
                try {
                    client.masterTakeover(myUrl,
                            state.getDataOf(".networkName"), masterId,
                            state.getDataOf(".masterLocation"));
                } catch (Exception e) {
                    logger.info("Client {} failed to acknowledge new master. " +
                    		"Removing {}", url);
                    removeParticipant(url);
                }
            }
        });
    }
}
