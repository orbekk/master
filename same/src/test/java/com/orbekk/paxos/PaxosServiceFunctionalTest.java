package com.orbekk.paxos;

import static org.junit.Assert.*;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.ConnectionManagerImpl;
import com.orbekk.same.http.RpcServlet;
import com.orbekk.same.http.JettyServerBuilder;
import com.orbekk.same.http.JettyServerContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PaxosServiceFunctionalTest {
    ConnectionManagerImpl connections = new ConnectionManagerImpl(500, 500);
    List<String> paxosUrls = new ArrayList<String>();
    JettyServerContainer server;
    String myUrl;
    int successfulProposals = 0;
    
    @Before
    public void setUp() throws Exception {
        JettyServerBuilder builder = new JettyServerBuilder(0);
        List<String> tempUrls = setupPaxos(builder, 10);
        server = builder.build();
        server.start();
        myUrl = "http://localhost:" + server.getPort();
        addUrls(tempUrls);
        System.out.println(paxosUrls);
    }
    
    @After
    public void tearDown() throws Exception {
        server.stop();
    }
    
    public List<String> setupPaxos(JettyServerBuilder builder, int instances) {
        List<String> tempUrls = new ArrayList<String>();
        for (int i = 1; i <= instances; i++) {
            JsonRpcServer jsonServer = new JsonRpcServer(
                    new PaxosServiceImpl("P" + i + ": "), PaxosService.class);
            String serviceId = "/PaxosService" + i + ".json";
            builder.withServlet(new RpcServlet(jsonServer), serviceId);
            tempUrls.add(serviceId);
        }
        return tempUrls;
    }
    
    public void addUrls(List<String> services) {
        for (String url : services) {
            paxosUrls.add(myUrl + url);
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
}
