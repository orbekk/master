package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.net.HttpUtil;
import com.orbekk.paxos.PaxosService;
import com.orbekk.paxos.PaxosServiceImpl;
import com.orbekk.same.config.Configuration;
import com.orbekk.same.http.ServerBuilder;
import com.orbekk.same.http.ServerContainer;
import com.orbekk.same.http.StateServlet;

public class SameController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int port;
    private ServerContainer server;
    private MasterServiceImpl master;
    private ClientServiceImpl client;
    private PaxosServiceImpl paxos;
    
    /**
     * Timeout for remote operations in milliseconds.
     */
    private static final int timeout = 10000;
    
    public static SameController create(Configuration configuration) {
        int port = configuration.getInt("port");
        ConnectionManagerImpl connections = new ConnectionManagerImpl(
                timeout, timeout);
        State state = new State("Default");
        Broadcaster broadcaster = BroadcasterImpl.getDefaultBroadcastRunner();
        
        String baseUrl = String.format("http://%s:%s/",
                configuration.get("localIp"), configuration.getInt("port"));
        
        String masterUrl = baseUrl + "MasterService.json";
        String clientUrl = baseUrl + "ClientService.json";
        
        MasterServiceImpl master = MasterServiceImpl.create(
                connections, broadcaster, masterUrl);
        
        ClientServiceImpl client = new ClientServiceImpl(state, connections,
                clientUrl);
        PaxosServiceImpl paxos = new PaxosServiceImpl("");
        
        ServerContainer server = new ServerBuilder(port)
                .withServlet(new StateServlet(), "/_/state")
                .withService(client.getService(), ClientService.class)
                .withService(master, MasterService.class)
                .withService(paxos, PaxosService.class)
                .build();
        
        SameController controller = new SameController(port, server, master, client,
                paxos);
        return controller;
    }
    
    public SameController(
            int port,
            ServerContainer server,
            MasterServiceImpl master,
            ClientServiceImpl client,
            PaxosServiceImpl paxos) {
        this.port = port;
        this.server = server;
        this.master = master;
        this.client = client;
        this.paxos = paxos;
    }

    public void start() throws Exception {
        server.start();
        master.start();
        client.start();
    }
    
    public void stop() {
        try {
            client.interrupt();
            master.interrupt();
            server.stop();
        } catch (Exception e) {
            logger.error("Failed to stop webserver", e);
        }
    }
    
    public void join() {
        try {
            server.join();
            master.join();
        } catch (InterruptedException e) {
            master.interrupt();
            try {
                server.stop();
            } catch (Exception e1) {
                logger.error("Failed to stop server", e);
            }
        }
    }
    
    public void joinNetwork(String url) {
        client.joinNetwork(url);
    }
    
    public ClientServiceImpl getClient() {
        return client;
    }
    
    public MasterServiceImpl getMaster() {
        return master;
    }
}
