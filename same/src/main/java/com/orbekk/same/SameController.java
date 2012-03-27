package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.paxos.PaxosService;
import com.orbekk.paxos.PaxosServiceImpl;
import com.orbekk.same.config.Configuration;
import com.orbekk.same.discovery.DirectoryService;
import com.orbekk.same.http.ServerContainer;
import com.orbekk.same.http.StateServlet;
import com.orbekk.same.http.JettyServerBuilder;
import com.orbekk.same.http.TjwsServerBuilder;

public class SameController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ServerContainer server;
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
            String masterUrl = configuration.get("baseUrl") +
                    "MasterService.json";
            master = Master.create(connections, serviceBroadcaster,
                    masterUrl, configuration.get("networkName"));
            if (lastKnownState == null) {
                lastKnownState = master.state;
            }
            master.resumeFrom(lastKnownState, masterId);
            master.start();
            masterService.setService(master.getService());
        }

        @Override
        public void disableMaster() {
            masterService.setService(null);
            if (master != null) {
                master.interrupt();
            }
        }
    };
    
    public static SameController create(Configuration configuration) {
        int port = configuration.getInt("port");
        ConnectionManagerImpl connections = new ConnectionManagerImpl(
                timeout, timeout);
        State clientState = new State(".InvalidClientNetwork");
        Broadcaster broadcaster = BroadcasterImpl.getDefaultBroadcastRunner();

        String baseUrl = String.format("http://%s:%s/",
                configuration.get("localIp"), configuration.getInt("port"));

        String masterUrl = baseUrl + "MasterService.json";
        String clientUrl = baseUrl + "ClientService.json";

        MasterServiceProxy master = new MasterServiceProxy();
//        Master master = Master.create(connections, broadcaster,
//                masterUrl, configuration.get("networkName"));

        Client client = new Client(clientState, connections,
                clientUrl, BroadcasterImpl.getDefaultBroadcastRunner());
        PaxosServiceImpl paxos = new PaxosServiceImpl("");

        StateServlet stateServlet = new StateServlet(client.getInterface(),
                new VariableFactory(client.getInterface()));

        ServerContainer server = new JettyServerBuilder(port)
            .withServlet(stateServlet, "/_/state")
            .withService(client.getService(), ClientService.class)
            .withService(master, MasterService.class)
            .withService(paxos, PaxosService.class)
            .build();

        SameController controller = new SameController(
                configuration, connections, server, master, client,
                paxos, broadcaster);
        return controller;
    }

    public SameController(
            Configuration configuration,
            ConnectionManager connections,
            ServerContainer server,
            MasterServiceProxy master,
            Client client,
            PaxosServiceImpl paxos,
            Broadcaster serviceBroadcaster) {
        this.configuration = configuration;
        this.connections = connections;
        this.server = server;
        this.masterService = master;
        this.client = client;
        this.paxos = paxos;
        this.serviceBroadcaster = serviceBroadcaster;
    }

    public void start() throws Exception {
        server.start();
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
        masterController.enableMaster(null, 1);
        String masterUrl = configuration.get("baseUrl") +
                "MasterService.json";
        joinNetwork(masterUrl);
    }

    public void joinNetwork(String url) {
        client.joinNetwork(url);
    }

    public Client getClient() {
        return client;
    }

    public Master getMaster() {
        return master;
    }
    
    public DirectoryService getDirectory() {
        String directoryUrl = configuration.get("directoryUrl");
        DirectoryService directory = null;
        if (directoryUrl != null) {
            directory = connections.getDirectory(directoryUrl);
        }
        return directory;
    }

    public VariableFactory createVariableFactory() {
        return new VariableFactory(client.getInterface());
    }
}
