package com.orbekk.same;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.net.HttpUtil;
import com.orbekk.paxos.PaxosService;
import com.orbekk.paxos.PaxosServiceImpl;
import com.orbekk.same.http.HandlerFactory;
import com.orbekk.same.http.RpcHandler;
import com.orbekk.same.http.ServerBuilder;
import com.orbekk.same.http.StateServlet;

public class SameController implements UrlReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int port;
    private Server server;
    private MasterServiceImpl master;
    private ClientServiceImpl client;
    private PaxosServiceImpl paxos;
    
    /**
     * Timeout for remote operations in milliseconds.
     */
    private static final int timeout = 10000;
    
    public static SameController create(int port) {
        ConnectionManagerImpl connections = new ConnectionManagerImpl(
                timeout, timeout);
        State state = new State("Default");
        Broadcaster broadcaster = BroadcasterImpl.getDefaultBroadcastRunner();
        
        MasterServiceImpl master = new MasterServiceImpl(state, connections,
                broadcaster);
        ClientServiceImpl client = new ClientServiceImpl(state, connections);
        PaxosServiceImpl paxos = new PaxosServiceImpl("");
        
        Server server = new ServerBuilder(port)
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
            Server server,
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
    
    public boolean tryGetUrl(String serverUrl) {
        int retries = 100;
        while (client.getUrl() == null && retries > 0) {
            HttpUtil.sendHttpRequest(serverUrl + "ping?port=" +
                    port);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return false;
            }
            retries -= 1;
        }
        return client.getUrl() != null;
    }
    
    public void joinNetwork(String url) {
            boolean hasUrl = tryGetUrl(url);
            if (hasUrl) {
                client.joinNetwork(url + "MasterService.json");
            }
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
    
    public ClientServiceImpl getClient() {
        return client;
    }
    
    public MasterServiceImpl getMaster() {
        return master;
    }
}
