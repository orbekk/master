package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.protobuf.Rpc;
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
    
    class RemoveParticipantIfFailsCallback<T> implements RpcCallback<T> {
        private final String participantLocation;
        private final Rpc rpc;

        public RemoveParticipantIfFailsCallback(
                String participantLocation, Rpc rpc) {
            this.participantLocation = participantLocation;
            this.rpc = rpc;
        }

        @Override
        public void run(T unused) {
            if (rpc.isOk()) {
                if (rpc.failed()) {
                    removeParticipant(participantLocation);
                }
            }
        }
    }
    
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
            sendFullStateThread.add(request.getLocation());
            addParticipant(request.getLocation());
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
    
    WorkQueue<String> updateStateRequestThread = new WorkQueue<String>() {
        @Override protected void onChange() {
            List<String> pending = getAndClear();
            List<Component> updatedComponents = new ArrayList<Component>();
            for (String component : pending) {
                updatedComponents.add(state.getComponent(component));
            }
            
            logger.info("updateStateRequestThread: Updated state: {}",
                    pending);
            for (String clientLocation : state.getList(
                    com.orbekk.same.State.PARTICIPANTS)) {
                sendComponents(clientLocation, updatedComponents);
            }
        }
    };

    public void sendComponents(String clientLocation,
            List<Component> components) {
        Services.Client client = connections.getClient0(clientLocation);
        if (client == null) {
            removeParticipant(clientLocation);
        }

        for (Component component : components) {
            Services.Component componentProto = componentToProto(component);
            Rpc rpc = new Rpc();
            RpcCallback<Empty> done =
                    new RemoveParticipantIfFailsCallback<Empty>(clientLocation,
                            rpc);
            client.setState(rpc, componentProto, done);
        }
    }
    
    WorkQueue<String> sendFullStateThread = new WorkQueue<String>() {
        @Override protected void onChange() {
            List<String> pending = getAndClear();
            logger.info("Sending full state to {}", pending);
            final List<Component> components = state.getComponents();
            for (String clientLocation : pending) {
                Services.Client client = connections.getClient0(clientLocation);
                if (client == null) {
                    removeParticipant(clientLocation);
                    continue;
                }
                
                { // Send masterTakeover().
                    Rpc rpc = new Rpc();
                    RpcCallback<Empty> done =
                            new RemoveParticipantIfFailsCallback<Empty>(
                                    clientLocation, rpc);
                    client.masterTakeover(rpc, getMasterInfo(), done);
                }
                sendComponents(clientLocation, components);
            }
        }
    };

    private Services.Component componentToProto(State.Component component) {
        return Services.Component.newBuilder()
                .setId(component.getName())
                .setData(component.getData())
                .setRevision(component.getRevision())
                .build();
    }
    
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
    
    private synchronized void addParticipant(String location) {
        List<String> participants = state.getList(State.PARTICIPANTS);
        if (!participants.contains(location)) {
            participants.add(location);
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
    
    /** This master should take over from an earlier master. */
    public void resumeFrom(State lastKnownState, final int masterId) {
        state = lastKnownState;
        state.update(".masterUrl", myUrl, state.getRevision(".masterUrl") + 100);
        state.update(".masterLocation", myLocation,
                state.getRevision(".masterLocation") + 100);
        this.masterId = masterId;
        
        for (final String location : state.getList(State.PARTICIPANTS)) {
            Services.Client client = connections.getClient0(location);
            final Rpc rpc = new Rpc();
            RpcCallback<Empty> done = new RpcCallback<Empty>() {
                @Override public void run(Empty unused) {
                    if (!rpc.isOk()) {
                        removeParticipant(location);
                    }
                }
            };
            if (client == null) {
                removeParticipant(location);
                continue;
            }
            client.masterTakeover(rpc, getMasterInfo(), done);
        }
    }
}
