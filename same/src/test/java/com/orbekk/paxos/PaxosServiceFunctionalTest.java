package com.orbekk.paxos;

import static org.junit.Assert.*;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.ConnectionManagerImpl;
import com.orbekk.same.RpcHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    int successfulProposals = 0;
    
    @Before
    public void setUp() throws Exception {
        server = TestServer.create(handler);
        myUrl = "http://localhost:" + server.port;
        setupPaxos(10);
    }
    
    public void setupPaxos(int instances) {
        for (int i = 1; i <= instances; i++) {
            JsonRpcServer jsonServer = new JsonRpcServer(
                    new PaxosServiceImpl("P" + i + ": "), PaxosService.class);
            String serviceId = "/PaxosService" + i + ".json";
            handler.addRpcServer(serviceId, jsonServer);
            paxosUrls.add(myUrl + serviceId);
        }
    }

    @Test
    public void testMasterElection() {
        MasterProposer m1 = new MasterProposer("http://client1", paxosUrls,
                connections);
        assertTrue(m1.propose(1));
    }

    @Test
    public void testWithCompetition() {
        int proposers = 5;
        List<Thread> masterProposers = new ArrayList<Thread>(); 
        for (int i = 1; i <= proposers; i++) {
            final int j = i;
            masterProposers.add(new Thread() {
                @Override public void run() {
                    MasterProposer client =
                            new MasterProposer("http:/client" + j, paxosUrls,
                                    connections);
                    if (client.proposeRetry(1)) {
                        incrementSuccessfulProposals(); 
                    }
                }
            });
        }
        for (Thread t : masterProposers) {
            t.start();
        }
        for (Thread t : masterProposers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
        assertEquals(5, successfulProposals);
    }

    public synchronized void incrementSuccessfulProposals() {
        successfulProposals += 1;
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
