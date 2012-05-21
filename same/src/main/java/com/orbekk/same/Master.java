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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.protobuf.Rpc;
import com.orbekk.same.Services.ClientState;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.FullStateResponse;
import com.orbekk.same.Services.MasterState;
import com.orbekk.same.Services.MasterTakeoverResponse;
import com.orbekk.same.State.Component;
import com.orbekk.util.RpcList;

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
            if (rpc.failed()) {
                removeParticipant(participantLocation);
            }
        }
    }
    
    private class MasterTakeover implements Runnable {
        final List<Services.ClientState> clientStates = new ArrayList<Services.ClientState>();
        final AtomicBoolean aborted = new AtomicBoolean(false);
        final List<String> clients = new CopyOnWriteArrayList<String>();
        final MasterState newMaster;
        
        private class TakeoverCallback implements RpcCallback<MasterTakeoverResponse> {
            final String client;

            public TakeoverCallback(String client) {
                this.client = client;
            }
            
            @Override public void run(MasterTakeoverResponse response) {
                if (response == null) {
                    clients.remove(client);
                } else if (!response.getSuccess()) {
                    aborted.set(true);
                } else {
                    clientStates.add(response.getClientState());
                }
            }
        }
        
        private class FullStateCallback implements RpcCallback<FullStateResponse> {
            @Override public void run(FullStateResponse response) {
                if (response != null) {
                    for (Services.Component componentPb : response.getComponentList()) {
                        state.update(componentPb.getId(), componentPb.getData(),
                                componentPb.getRevision());
                        updateRevision(componentPb.getRevision());
                    }
                }
            }
        }
        
        private class RemoveClientCallback<T> implements RpcCallback<T> {
            final String client;
            
            public RemoveClientCallback(String client) {
                this.client = client;
            }
            
            @Override public void run(T response) {
                if (response == null) {
                    clients.remove(client);
                }
            }
        }
        
        public MasterTakeover(List<String> clients, MasterState newMaster) {
            this.clients.addAll(clients);
            this.newMaster = newMaster;
        }

        private void sendTakeovers() throws InterruptedException {
            RpcList rpcs = new RpcList();
            for (String location : clients) {
                Services.Client client = connections.getClient0(location);
                if (client == null) {
                    clients.remove(location);
                } else {
                    Rpc rpc = rpcf.create();
                    client.masterTakeover(rpc, newMaster, new TakeoverCallback(location));
                    rpcs.add(rpc);
                }
            }
            rpcs.awaitAll();
        }
        
        private ClientState getBestClient(List<ClientState> clients) {
            if (clients.isEmpty()) {
                return null;
            }
            ClientState best = clients.get(0);
            for (ClientState client : clients) {
                if (client.getRevision() > best.getRevision()) {
                    best = client;
                }
            }
            return best;
        }
        
        private void getMostRecentState() throws InterruptedException {
            boolean successful = false;
            while (!successful && !aborted.get()) {
                ClientState bestClient = getBestClient(clientStates);
                if (bestClient == null) {
                    successful = true;
                    continue;
                }
                Services.Client client = connections.getClient0(bestClient.getLocation());
                if (client == null) {
                    clients.remove(bestClient.getLocation());
                    continue;
                }
                
                Rpc rpc = rpcf.create();
                FullStateCallback done = new FullStateCallback();
                client.getFullState(rpc, Empty.getDefaultInstance(), done);
                rpc.await();
                successful = rpc.isOk();
                
                if (!successful) {
                    clients.remove(bestClient.getLocation());
                }
            }
        }
        
        private void updateParticipants() throws InterruptedException {
            long newRevision = revision.incrementAndGet();
            state.updateFromObject(State.PARTICIPANTS, clients, newRevision);
        }
        
        private void sendFullState() throws InterruptedException {
            RpcList rpcs = new RpcList();
            for (String location : clients) {
                Services.Client client = connections.getClient0(location);
                if (client == null) {
                    clients.remove(client);
                    continue;
                }
                RemoveClientCallback<Empty> done = new RemoveClientCallback<Empty>(location);
                for (Component component : state.getComponents()) {
                    Services.Component componentPb = ServicesPbConversion.componentToPb(component);
                    Rpc rpc = rpcf.create();
                    client.setState(rpc, componentPb, done);
                    rpcs.add(rpc);
                }
            }
            rpcs.awaitAll();
        }
        
        private void finishTakeover() throws InterruptedException {
            RpcList rpcs = new RpcList();
            for (String location : clients) {
                Services.Client client = connections.getClient0(location);
                if (client == null) {
                    clients.remove(client);
                    continue;
                }
                RemoveClientCallback<Empty> done = new RemoveClientCallback<Empty>(location);
                Rpc rpc = rpcf.create();
                client.masterTakeoverFinished(rpc, newMaster, done);
                rpcs.add(rpc);
            }
            rpcs.awaitAll();
        }
        
        @Override public void run() {
            try {
                sendTakeovers();
                getMostRecentState();
                updateParticipants();
                sendFullState();
                finishTakeover();
            } catch (InterruptedException e) {
                // Abort master takeover.
                logger.warn("Master takeover aborted: ", e);
                aborted.set(true);
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
            sendInitialMasterTakeover(request.getLocation());
            addParticipant(request.getLocation());
            done.run(Empty.getDefaultInstance());
        }

        @Override public void updateStateRequest(RpcController controller,
                Services.Component request,
                RpcCallback<Services.UpdateComponentResponse> done) {
            boolean success = false;
            if (state.checkRevision(request.getId(), request.getRevision())) {
                success = true;
                long newRevision = revision.incrementAndGet();
                state.forceUpdate(request.getId(), request.getData(), newRevision);
                sendStateToClients(state.getComponent(request.getId()));
            }
            done.run(Services.UpdateComponentResponse.newBuilder()
                    .setSuccess(success).build());
        }
    };
    
    private void sendStateToClients(State.Component component) {
        for (String clientLocation : state.getList(
                com.orbekk.same.State.PARTICIPANTS)) {
            sendComponent(clientLocation, component);
        }
    }
    
    private void sendComponent(String clientLocation, Component component) {
        Services.Client client = connections.getClient0(clientLocation);
        if (client == null) {
            removeParticipant(clientLocation);
        }

        Services.Component componentProto = ServicesPbConversion.componentToPb(component);
        Rpc rpc = rpcf.create();
        RpcCallback<Empty> done =
                new RemoveParticipantIfFailsCallback<Empty>(clientLocation,
                        rpc);
        client.setState(rpc, componentProto, done);
    }
    
    private void sendComponents(String clientLocation,
            List<Component> components) {
        Services.Client client = connections.getClient0(clientLocation);
        if (client == null) {
            removeParticipant(clientLocation);
        }

        for (Component component : components) {
            Services.Component componentProto = ServicesPbConversion.componentToPb(component);
            Rpc rpc = rpcf.create();
            RpcCallback<Empty> done =
                    new RemoveParticipantIfFailsCallback<Empty>(clientLocation,
                            rpc);
            client.setState(rpc, componentProto, done);
        }
    }
    
    private void sendFullState(String clientLocation) {
        List<Component> components = state.getComponents();
        sendComponents(clientLocation, components);
    }
    
    private synchronized void sendInitialMasterTakeover(String clientLocation) {
        Services.Client client = connections.getClient0(clientLocation);
        
        // Step 1: Send takeover.
        Rpc rpc1 = rpcf.create();
        RpcCallback<MasterTakeoverResponse> done1 =
                new RemoveParticipantIfFailsCallback<MasterTakeoverResponse>(
                        clientLocation, rpc1);
        client.masterTakeover(rpc1, getMasterInfo(), done1);
        
        // Step 2: Send all state.
        sendFullState(clientLocation);
        
        // Step 3: Finish takeover.
        Rpc rpc2 = rpcf.create();
        RpcCallback<Empty> done2 = new RemoveParticipantIfFailsCallback<Empty>(
                clientLocation, rpc2);
        client.masterTakeoverFinished(rpc2, getMasterInfo(), done2);
    }
    
    void performWork() {
    }

    public void start() {
    }

    public void interrupt() {
    }

    public Services.Master getNewService() {
        return newMasterImpl;
    }
    
    private synchronized void addParticipant(String location) {
        List<String> participants = state.getList(State.PARTICIPANTS);
        if (!participants.contains(location)) {
            participants.add(location);
            long newRevision = revision.incrementAndGet();
            state.updateFromObject(State.PARTICIPANTS, participants,
                    newRevision);
            sendStateToClients(state.getComponent(State.PARTICIPANTS));
        }
        
    }

    private synchronized void removeParticipant(String url) {
        List<String> participants0 = state.getList(State.PARTICIPANTS);
        if (participants0.contains(url)) {
            logger.info("removeParticipant({})", url);
            participants0.remove(url);
            long newRevision = revision.incrementAndGet();
            state.updateFromObject(State.PARTICIPANTS, participants0, 
                    newRevision);
            sendStateToClients(state.getComponent(State.PARTICIPANTS));
        }
    }
    
    /** This master should take over from an earlier master. */
    public void resumeFrom(State lastKnownState, final int masterId) {
        for (Component c : lastKnownState.getComponents()) {
            updateRevision(c.getRevision());
        }
        state = lastKnownState;
        this.masterId = masterId;
        MasterTakeover takeover = new MasterTakeover(
                state.getList(State.PARTICIPANTS), getMasterInfo());
        takeover.run();
    }
    
    public void updateRevision(long newRevision) {
        boolean updated = false;
        while (!updated) {
            long expected = revision.get();
            long update = Math.max(expected, newRevision);
            updated = revision.compareAndSet(expected, update);
        }
    }
}
