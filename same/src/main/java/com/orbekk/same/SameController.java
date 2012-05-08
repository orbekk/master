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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.paxos.PaxosServiceImpl;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.SimpleProtobufServer;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.MasterState;
import com.orbekk.same.Services.SystemStatus;
import com.orbekk.same.config.Configuration;

public class SameController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SimpleProtobufServer pServer;
    private volatile Master master;
    private final Client client;
    private final PaxosServiceImpl paxos;
    private final Configuration configuration;
    private final ConnectionManager connections;
    private final RpcFactory rpcf;

    /**
     * Timeout for remote operations in milliseconds.
     */
    private static final int timeout = 10000;

    private class SystemServiceImpl extends Services.SystemService {

        
        private void addMasterInfo(SystemStatus.Builder response) {
            Master currentMaster = master;
            if (currentMaster != null) {
                response.setMasterStatus(currentMaster.getMasterInfo());
                State masterState = new State(currentMaster.state);
                response.addAllMasterStateComponent(
                        ServicesPbConversion.componentsToPb(masterState.getComponents()));
            }
        }
        
        public void addClientInfo(SystemStatus.Builder response) {
            response.setClientStatus(client.getClientState());
            if (client.getMaster() != null) {
                response.setClientMasterStatus(client.getMaster());
            }
            State clientState = new State(client.state);
            response.addAllClientStateComponent(
                    ServicesPbConversion.componentsToPb(clientState.getComponents()));
        }
        
        @Override
        public void getSystemStatus(RpcController rpc, Empty request,
                RpcCallback<SystemStatus> done) {
            SystemStatus.Builder response = SystemStatus.newBuilder();
            addMasterInfo(response);
            addClientInfo(response);
            done.run(response.build());
        }
    }

    private MasterController masterController = new MasterController() {
        @Override
        public void enableMaster(String networkName,
                State lastKnownState, int masterId) {
            String myLocation = configuration.get("localIp") + ":" +
                    configuration.get("pport");
            String masterUrl = configuration.get("baseUrl") +
                    "MasterService.json";
            master = Master.create(connections,
                    masterUrl, configuration.get("networkName"), myLocation,
                    rpcf);
            master.resumeFrom(lastKnownState, masterId);
            pServer.registerService(master.getNewService());
            master.start();
            registerNetwork(master);
        }

        @Override
        public void disableMaster() {
            if (master != null) {
                pServer.removeService(master.getNewService());
                master.interrupt();
                master = null;
            }
        }
    };
    
    public static void enableRpcLogging() {
        java.util.logging.Level level = java.util.logging.Level.FINEST;
        java.util.logging.Logger rpcLog = java.util.logging.Logger.getLogger(
                com.orbekk.protobuf.RequestDispatcher.class.getName());
        rpcLog.setLevel(level);
        java.util.logging.Logger channelLog = java.util.logging.Logger.getLogger(
                com.orbekk.protobuf.RpcChannel.class.getName());
        channelLog.setLevel(level);
        java.util.logging.Handler handler = new java.util.logging.ConsoleHandler();
        handler.setLevel(level);
        rpcLog.addHandler(handler);
        channelLog.addHandler(handler);
    }
    
    public static SameController create(Configuration configuration) {
        enableRpcLogging();
        int pport = configuration.getInt("pport");
        String myLocation = configuration.get("localIp") + ":" + pport;
        
        ConnectionManagerImpl connections = new ConnectionManagerImpl(
                timeout, timeout);
        RpcFactory rpcf = new RpcFactory(timeout);
        
        State clientState = new State();
        String baseUrl = String.format("http://%s:%s/",
                configuration.get("localIp"), configuration.getInt("port"));
        String clientUrl = baseUrl + "ClientService.json";

        ExecutorService clientExecutor = Executors.newCachedThreadPool();
        Client client = new Client(clientState, connections,
                clientUrl, myLocation, rpcf, clientExecutor);
        PaxosServiceImpl paxos = new PaxosServiceImpl("");
        
        SimpleProtobufServer pServer = SimpleProtobufServer.create(pport);
        pServer.registerService(client.getNewService());
        pServer.registerService(paxos.getService());
        
        SameController controller = new SameController(
                configuration, connections, client,
                paxos, pServer, rpcf);
        
        pServer.registerService(controller.new SystemServiceImpl());
        return controller;
    }

    public SameController(
            Configuration configuration,
            ConnectionManager connections,
            Client client,
            PaxosServiceImpl paxos,
            SimpleProtobufServer pServer,
            RpcFactory rpcf) {
        this.configuration = configuration;
        this.connections = connections;
        this.client = client;
        this.paxos = paxos;
        this.pServer = pServer;
        this.rpcf = rpcf;
    }

    public void start() throws Exception {
        pServer.start();
        client.setMasterController(masterController);
        client.start();
    }

    public void stop() {
        try {
            client.interrupt();
            if (master != null) {
                master.interrupt();
            }
            pServer.interrupt();
        } catch (Exception e) {
            logger.error("Failed to stop webserver", e);
        }
    }

    public void join() {
        client.interrupt();
        if (master != null) {
            master.interrupt();
        }
    }

    public void createNetwork(String networkName) {
        masterController.disableMaster();
        masterController.enableMaster(networkName, new State(), 1);
        joinNetwork(master.getMasterInfo());
    }

    public void joinNetwork(Services.MasterState masterInfo) {
        client.joinNetwork(masterInfo);
    }

    public Client getClient() {
        return client;
    }

    public Master getMaster() {
        return master;
    }
    
    public void registerCurrentNetwork() {
        registerNetwork(master);
    }
    
    public void registerNetwork(Master master) {
        Services.Directory directory = getDirectory();
        if (directory == null) {
            return;
        }
        Services.MasterState request = Services.MasterState.newBuilder()
                .setNetworkName(master.getNetworkName())
                .setMasterLocation(master.getLocation())
                .build();
        final Rpc rpc = rpcf.create();
        RpcCallback<Services.Empty> done = new RpcCallback<Services.Empty>() {
            @Override public void run(Services.Empty unused) {
                if (!rpc.isOk()) {
                    logger.warn("Failed to register network: {}", rpc);
                }
            }
        };
        directory.registerNetwork(rpc, request, done);
    }
    
    public Services.Directory getDirectory() {
        String directoryLocation = configuration.get("directoryLocation");
        if (directoryLocation != null) {
            return connections.getDirectory(directoryLocation);
        } else {
            return null;
        }
    }

    public VariableFactory createVariableFactory() {
        return new VariableFactory(client.getInterface());
    }
    
    public void killMaster() {
        masterController.disableMaster();
    }
}
