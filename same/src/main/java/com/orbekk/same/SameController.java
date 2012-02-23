package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.net.BroadcasterInterface;
import com.orbekk.net.BroadcastListener;
import com.orbekk.net.BroadcasterFactory;
import com.orbekk.net.DefaultBroadcasterFactory;
import com.orbekk.paxos.PaxosService;
import com.orbekk.paxos.PaxosServiceImpl;
import com.orbekk.same.config.Configuration;
import com.orbekk.same.http.ServerBuilder;
import com.orbekk.same.http.ServerContainer;
import com.orbekk.same.http.StateServlet;

public class SameController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ServerContainer server;
    private Master master;
    private Client client;
    private PaxosServiceImpl paxos;
    private DiscoveryService discoveryService;
    private BroadcasterFactory broadcasterFactory;
    private Configuration configuration;
    
    /**
     * Timeout for remote operations in milliseconds.
     */
    private static final int timeout = 10000;
    
    public static SameController create(BroadcasterFactory broadcasterFactory,
            Configuration configuration) {
        int port = configuration.getInt("port");
        ConnectionManagerImpl connections = new ConnectionManagerImpl(
                timeout, timeout);
        State clientState = new State(".InvalidClientNetwork");
        Broadcaster broadcaster = BroadcasterImpl.getDefaultBroadcastRunner();
        
        String baseUrl = String.format("http://%s:%s/",
                configuration.get("localIp"), configuration.getInt("port"));
        
        String masterUrl = baseUrl + "MasterService.json";
        String clientUrl = baseUrl + "ClientService.json";
        
        Master master = Master.create(connections, broadcaster,
                masterUrl, configuration.get("networkName"));
        
        Client client = new Client(clientState, connections,
                clientUrl);
        PaxosServiceImpl paxos = new PaxosServiceImpl("");
        
        DiscoveryService discoveryService = null;
        if ("true".equals(configuration.get("enableDiscovery"))) {
            BroadcastListener broadcastListener = new BroadcastListener(
                    configuration.getInt("discoveryPort"));
            discoveryService = new DiscoveryService(client, broadcastListener);
        }
        
        StateServlet stateServlet = new StateServlet(client.getInterface(),
                new VariableFactory(client.getInterface()));
        
        ServerContainer server = new ServerBuilder(port)
        .withServlet(stateServlet, "/_/state")
        .withService(client.getService(), ClientService.class)
        .withService(master.getService(), MasterService.class)
        .withService(paxos, PaxosService.class)
        .build();
        
        SameController controller = new SameController(
                configuration, server, master, client,
                paxos, discoveryService, broadcasterFactory);
        return controller;
    }
    
    public static SameController create(Configuration configuration) {
        return create(new DefaultBroadcasterFactory(), configuration);
    }
    
    public SameController(
            Configuration configuration,
            ServerContainer server,
            Master master,
            Client client,
            PaxosServiceImpl paxos,
            DiscoveryService discoveryService,
            BroadcasterFactory broadcasterFactory) {
        this.configuration = configuration;
        this.server = server;
        this.master = master;
        this.client = client;
        this.paxos = paxos;
        this.discoveryService = discoveryService;
        this.broadcasterFactory = broadcasterFactory;
    }

    public void start() throws Exception {
        server.start();
        master.start();
        client.start();
        if (discoveryService != null) {
            discoveryService.start();
        }
    }
    
    public void stop() {
        try {
            client.interrupt();
            master.interrupt();
            server.stop();
            if (discoveryService != null) {
                discoveryService.interrupt();
            }
        } catch (Exception e) {
            logger.error("Failed to stop webserver", e);
        }
    }
    
    public void join() {
        try {
            server.join();
            client.interrupt();
            master.interrupt();
            if (discoveryService != null) {
                discoveryService.join();
            }
        } catch (InterruptedException e) {
            try {
                server.stop();
            } catch (Exception e1) {
                logger.error("Failed to stop server", e);
            }
        }
    }
    
    public void searchNetworks() {
        BroadcasterInterface broadcaster = broadcasterFactory.create();
        String message = "Discover " + client.getUrl();
        broadcaster.sendBroadcast(configuration.getInt("discoveryPort"),
                message.getBytes());
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
    
    public VariableFactory createVariableFactory() {
        return new VariableFactory(client.getInterface());
    }
}
