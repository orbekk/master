package com.orbekk.paxos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.protobuf.SimpleProtobufServer;
import com.orbekk.same.ConnectionManagerImpl;
import com.orbekk.same.Services.ClientState;
import com.orbekk.same.http.JettyServerBuilder;
import com.orbekk.same.http.JettyServerContainer;
import com.orbekk.same.http.RpcServlet;

public class PaxosServiceFunctionalTest {
    ConnectionManagerImpl connections = new ConnectionManagerImpl(500, 500);
    List<String> paxosUrls = new ArrayList<String>();
    List<SimpleProtobufServer> servers = new ArrayList<SimpleProtobufServer>();
    String myUrl;
    int successfulProposals = 0;
    ClientState client1 = ClientState.newBuilder()
            .setLocation("client1Location")
            .build();
    
    Runnable sleepForever = new Runnable() {
        @Override public synchronized void run() {
            while (!Thread.interrupted()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    };
    
    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 11; i++) {
            setupPaxos(i);
        }
    }
    
    @After
    public void tearDown() throws Exception {
        for (SimpleProtobufServer server : servers) {
            server.interrupt();
        }
    }
    
    public void setupPaxos(int i) {
        SimpleProtobufServer server = SimpleProtobufServer.create(0);
        server.registerService(new PaxosServiceImpl("P: " + i + ": ").getService());
        server.start();
        servers.add(server);
        String location = "localhost:" + server.getPort();
        paxosUrls.add(location);
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
    public void testMasterElection() throws InterruptedException {
        MasterProposer m1 = new MasterProposer(client1, paxosUrls,
                connections);
        assertTrue(m1.propose(1));
    }
    
    @Test
    public void testMasterElectionTask() throws InterruptedException, ExecutionException {
        MasterProposer m1 = new MasterProposer(client1, paxosUrls,
                connections);
        Future<Integer> result = m1.startProposalTask(1, null);
        assertEquals(new Integer(1), result.get());
    }
    
    @Test
    public void cancelledElection() throws InterruptedException {
        MasterProposer m1 = new MasterProposer(client1, paxosUrls,
                connections);
        assertTrue(m1.propose(1));

        Future<Integer> result = m1.startProposalTask(1, sleepForever);
        result.cancel(true);
        assertTrue(result.isCancelled());
    }

    @Test
    public void testOnlyOneCompletes() throws InterruptedException, ExecutionException {
        MasterProposer m1 = new MasterProposer(client1, paxosUrls,
                connections);
        ClientState client2 = ClientState.newBuilder().setLocation("client2").build();
        MasterProposer m2 = new MasterProposer(client2, paxosUrls,
                connections);
        final Future<Integer> result1 = m1.startProposalTask(1, sleepForever);
        final Future<Integer> result2 = m2.startProposalTask(1, sleepForever);
        
        Thread t1 = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    result1.get();
                    result2.cancel(true);
                } catch (CancellationException e) {
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
            }
        });
        
        Thread t2 = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    result2.get();
                    result1.cancel(true);
                } catch (CancellationException e) {
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                }
            }
        });
        
        t1.start();
        t2.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
        }
        
        assertTrue(result1.isCancelled() || result2.isCancelled());
        if (!result1.isCancelled()) {
            assertEquals(new Integer(1), result1.get());
        }
        if (!result2.isCancelled()) {
            assertEquals(new Integer(1), result2.get());
        }
    }
    
    @Test
    public void testWithCompetition() {
        int proposers = 5;
        List<Thread> masterProposers = new ArrayList<Thread>(); 
        for (int i = 1; i <= proposers; i++) {
            final int j = i;
            masterProposers.add(new Thread() {
                @Override public void run() {
                    ClientState client = ClientState.newBuilder()
                            .setLocation("client" + j)
                            .build();
                    MasterProposer proposer =
                            new MasterProposer(client, paxosUrls,
                                    connections);
                    try {
                        if (proposer.proposeRetry(1)) {
                            incrementSuccessfulProposals(); 
                        }
                    } catch (InterruptedException e) {
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
