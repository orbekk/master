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
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.protobuf.Rpc;
import com.orbekk.same.Services.ClientState;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.State.Component;
import com.orbekk.util.WorkQueue;

public class Master {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ConnectionManager connections;
    private String myLocation; // Protobuf server location, i.e., myIp:port
    private String networkName;
    private AtomicLong revision = new AtomicLong(1);
    State state;
    private volatile int masterId = 1;
    private final RpcFactory rpcf;
    
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
            String myUrl, String networkName,
            String myLocation, RpcFactory rpcf) {
        State state = new State();
        return new Master(state, connections, networkName, myLocation, rpcf);
    }

    Master(State initialState, ConnectionManager connections,
            String networkName, String myLocation, RpcFactory rpcf) {
        this.state = initialState;
        this.connections = connections;
        this.myLocation = myLocation;
        this.networkName = networkName;
        this.rpcf = rpcf;
    }
    
    public String getNetworkName() {
        return networkName;
    }
    
    public String getLocation() {
        return myLocation;
    }

    public Services.MasterState getMasterInfo() {
        return Services.MasterState.newBuilder()
                .setMasterLocation(getLocation())
                .setNetworkName(getNetworkName())
                .setMasterId(masterId)
                .setRevision(revision.get())
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
            boolean success = false;
            if (state.checkRevision(request.getId(), request.getRevision())) {
                success = true;
                long newRevision = revision.incrementAndGet();
                state.forceUpdate(request.getId(), request.getData(), newRevision);
                updateStateRequestThread.add(request.getId());
            }
            done.run(Services.UpdateComponentResponse.newBuilder()
                    .setSuccess(success).build());
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
            Rpc rpc = rpcf.create();
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
                    Rpc rpc = rpcf.create();
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
        this.masterId = masterId;
        
        for (final String location : state.getList(State.PARTICIPANTS)) {
            Services.Client client = connections.getClient0(location);
            final Rpc rpc = rpcf.create();
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
