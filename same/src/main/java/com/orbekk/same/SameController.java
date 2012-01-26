package com.orbekk.same;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.paxos.PaxosService;
import com.orbekk.paxos.PaxosServiceImpl;

public class SameController implements UrlReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;
    private MasterServiceImpl master;
    private ClientServiceImpl client;
    private PaxosServiceImpl paxos;
    
    /**
     * Timeout for remote operations in milliseconds.
     */
    private static final int timeout = 2000;
    
    public static SameController create(int port) {
        ConnectionManagerImpl connections = new ConnectionManagerImpl(
                timeout, timeout);
        State state = new State("Default");
        Broadcaster broadcaster = BroadcasterImpl.getDefaultBroadcastRunner();
        
        MasterServiceImpl master = new MasterServiceImpl(state, connections,
                broadcaster);
        JsonRpcServer jsonMaster = new JsonRpcServer(master, MasterService.class);
        ClientServiceImpl client = new ClientServiceImpl(state, connections);
        JsonRpcServer jsonClient = new JsonRpcServer(client, ClientService.class);
        PaxosServiceImpl paxos = new PaxosServiceImpl("");
        JsonRpcServer jsonPaxos = new JsonRpcServer(paxos, PaxosService.class);
        
        Server server = new Server(port);
        SameController controller = new SameController(server, master, client,
                paxos);
        RpcHandler rpcHandler = new RpcHandler(controller);
        
        rpcHandler.addRpcServer("/MasterService.json", jsonMaster);
        rpcHandler.addRpcServer("/ClientService.json", jsonClient);
        rpcHandler.addRpcServer("/PaxosService.json", jsonPaxos);
        
        server.setHandler(rpcHandler);
        return controller;
    }
    
    public SameController(
            Server server,
            MasterServiceImpl master,
            ClientServiceImpl client,
            PaxosServiceImpl paxos) {
        this.server = server;
        this.master = master;
        this.paxos = paxos;
    }

    public void start() throws Exception {
        server.start();
        master.start();
    }
    
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            logger.error("Failed to stop webserver", e);
            master.interrupt();
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
        client.joinNetwork(url + "MasterService.json");
    }
    
    @Override
    public void setUrl(String url) {
        if (master != null) {
            master.setUrl(url);
        }
        if (client != null) {
            client.setUrl(url);
        }
    }
    
}