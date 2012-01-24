package com.orbekk.paxos;

import static org.junit.Assert.*;

import com.orbekk.same.TestConnectionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PaxosServiceTest {
    TestConnectionManager connections = new TestConnectionManager();
    private PaxosServiceImpl p1 = new PaxosServiceImpl("P1: "); 
    private PaxosServiceImpl p2 = new PaxosServiceImpl("P2: "); 
    private PaxosServiceImpl p3 = new PaxosServiceImpl("P3: "); 
    private PaxosServiceImpl p4 = new PaxosServiceImpl("P4: "); 
    private PaxosServiceImpl p5 = new PaxosServiceImpl("P5: "); 
    private String client = "client";
    private String client1 = "client1";
    private String client2 = "client2";
    private String client3 = "client3";
    private String client4 = "client4";
    private String client5 = "client5";
    private List<PaxosServiceImpl> servers = new ArrayList<PaxosServiceImpl>();

    @Before
    public void setUp() {
        Collections.addAll(servers, p1, p2, p3, p4, p5);    
        connections.paxosMap.put("p1", p1);
        connections.paxosMap.put("p2", p2);
        connections.paxosMap.put("p3", p3);
        connections.paxosMap.put("p4", p4);
        connections.paxosMap.put("p5", p5);
    }

    @Test
    public void simpleCase() {
        assertTrue(p1.propose(client, 1));
        assertTrue(p1.acceptRequest(client, 1));
    }

    @Test
    public void lowerProposalFails() {
        assertTrue(p1.propose(client1, 10));
        assertFalse(p1.propose(client2, 9));
        assertTrue(p1.propose(client2, 100));
    }

    @Test
    public void testAccept() {
        assertTrue(p1.propose(client1, 3));
        assertTrue(p1.propose(client2, 4));
        assertFalse(p1.acceptRequest(client1, 3));
        assertTrue(p1.acceptRequest(client2, 4));
    }

    public List<String> paxosUrls() {
        return new ArrayList<String>(connections.paxosMap.keySet());
    }

    @Test
    public void integrationTest() {
        MasterProposer proposer = new MasterProposer("client1", paxosUrls(),
                connections);
        assertTrue(proposer.propose(1));
    }
}
