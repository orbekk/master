package com.orbekk.paxos;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.ConnectionManagerImpl;
import com.orbekk.same.RpcHandler;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

public class PaxosServiceFunctionalTest {
    ConnectionManagerImpl connections = new ConnectionManagerImpl(500, 500);
    List<String> paxosUrls = new ArrayList<String>();
    RpcHandler handler = new RpcHandler(null);
    TestServer server;
    String myUrl;

    @Before
    public void setUp() throws Exception {
        server = TestServer.create(handler);
        myUrl = "http://localhost:" + server.port + "/";
        setupPaxos(5);
    }
    
    public void setupPaxos(int instances) {
        for (int i = 1; i <= instances; i++) {
            JsonRpcServer jsonServer = new JsonRpcServer(
                    new PaxosServiceImpl("" + i), PaxosService.class);
            String serviceId = "/PaxosService" + i + ".json";
            handler.addRpcServer(serviceId, jsonServer);
        }
    }

    @Test
    public void nullTest() {
    }

    public static class TestServer {
        public Server server;
        public int port;

        public static TestServer create(Handler handler) throws Exception {
            Server server = new Server(0);
            server.setHandler(handler);
            server.start();
            int port = server.getConnectors()[0].getLocalPort();
            return new TestServer(server, port);
        }

        private TestServer(Server server, int port) {
            this.server = server;
            this.port = port;
        }
    }
}
