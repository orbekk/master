package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.orbekk.paxos.PaxosService;
import com.orbekk.paxos.PaxosServiceImpl;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.SimpleProtobufServer;
import com.orbekk.same.config.Configuration;
import com.orbekk.same.http.JettyServerBuilder;
import com.orbekk.same.http.ServerContainer;
import com.orbekk.same.http.StateServlet;

public class SameController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ServerContainer server;
    private SimpleProtobufServer pServer;
    private MasterServiceProxy masterService;
    private Master master;
    private Client client;
    private PaxosServiceImpl paxos;
    private Configuration configuration;
    private ConnectionManager connections;
    private Broadcaster serviceBroadcaster;

    /**
     * Timeout for remote operations in milliseconds.
     */
    private static final int timeout = 10000;

    private MasterController masterController = new MasterController() {
        @Override
        public void enableMaster(State lastKnownState, int masterId) {
            String myLocation = configuration.get("localIp") + ":" +
                    configuration.get("pport");
            String masterUrl = configuration.get("baseUrl") +
                    "MasterService.json";
            master = Master.create(connections, serviceBroadcaster,
                    masterUrl, configuration.get("networkName"), myLocation);
            master.resumeFrom(lastKnownState, masterId);
            pServer.registerService(master.getNewService());
            master.start();
        }

        @Override
        public void disableMaster() {
            if (master != null) {
                master.interrupt();
            }
        }
    };
    
    public static SameController create(Configuration configuration) {
        int port = configuration.getInt("port");
        int pport = configuration.getInt("pport");
        String myLocation = configuration.get("localIp") + ":" + pport;
        
        ConnectionManagerImpl connections = new ConnectionManagerImpl(
                timeout, timeout);
        State clientState = new State(".InvalidClientNetwork");
        Broadcaster broadcaster = BroadcasterImpl.getDefaultBroadcastRunner();
        String baseUrl = String.format("http://%s:%s/",
                configuration.get("localIp"), configuration.getInt("port"));
        String clientUrl = baseUrl + "ClientService.json";

        MasterServiceProxy master = new MasterServiceProxy();
        Client client = new Client(clientState, connections,
                clientUrl, myLocation,
                BroadcasterImpl.getDefaultBroadcastRunner());
        PaxosServiceImpl paxos = new PaxosServiceImpl("");
        StateServlet stateServlet = new StateServlet(client.getInterface(),
                new VariableFactory(client.getInterface()));
        ServerContainer server = new JettyServerBuilder(port)
            .withServlet(stateServlet, "/_/state")
            .withService(client.getService(), ClientService.class)
            .withService(master, MasterService.class)
            .withService(paxos, PaxosService.class)
            .build();
        
        SimpleProtobufServer pServer = SimpleProtobufServer.create(pport);
        
        SameController controller = new SameController(
                configuration, connections, server, master, client,
                paxos, broadcaster, pServer);
        return controller;
    }

    public SameController(
            Configuration configuration,
            ConnectionManager connections,
            ServerContainer server,
            MasterServiceProxy master,
            Client client,
            PaxosServiceImpl paxos,
            Broadcaster serviceBroadcaster,
            SimpleProtobufServer pServer) {
        this.configuration = configuration;
        this.connections = connections;
        this.server = server;
        this.masterService = master;
        this.client = client;
        this.paxos = paxos;
        this.serviceBroadcaster = serviceBroadcaster;
        this.pServer = pServer;
    }

    public void start() throws Exception {
        server.start();
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
            server.stop();
            pServer.interrupt();
        } catch (Exception e) {
            logger.error("Failed to stop webserver", e);
        }
    }

    public void join() {
        server.join();
        client.interrupt();
        if (master != null) {
            master.interrupt();
        }
    }

    public void createNetwork(String networkName) {
        masterController.disableMaster();
        masterController.enableMaster(new State(networkName), 1);
        String masterUrl = configuration.get("baseUrl") +
                "MasterService.json";
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
    
    public void registerNetwork(Master master) {
        Services.Directory directory = getDirectory();
        if (directory == null) {
            return;
        }
        Services.MasterState request = Services.MasterState.newBuilder()
                .setNetworkName(master.getNetworkName())
                .setMasterUrl(master.getUrl())
                .setMasterLocation(master.getLocation())
                .build();
        final Rpc rpc = new Rpc();
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
}
