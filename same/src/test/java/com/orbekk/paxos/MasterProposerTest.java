package com.orbekk.paxos;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.orbekk.same.TestConnectionManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MasterProposerTest {
    TestConnectionManager connections = new TestConnectionManager();
    PaxosService p1 = mock(PaxosService.class);
    PaxosService p2 = mock(PaxosService.class);
    PaxosService p3 = mock(PaxosService.class);
    PaxosService p4 = mock(PaxosService.class);
    PaxosService p5 = mock(PaxosService.class);
    
    @Before public void setUp() {
    }
    
    List<String> paxosUrls() {
        List<String> urls = new ArrayList<String>();
        urls.addAll(connections.paxosMap.keySet());
        return urls;
    }

    @Test public void successfulProposal() throws Exception {
        connections.paxosMap.put("p1", p1);
        when(p1.propose("client1", 1)).thenReturn(1);
        when(p1.acceptRequest("client1", 1)).thenReturn(1);
        
        MasterProposer c1 = new MasterProposer(
                "client1",
                paxosUrls(),
                connections);
        assertTrue(c1.propose(1));
    }

    @Test public void unsucessfulProposal() throws Exception {
        connections.paxosMap.put("p1", p1);
        when(p1.propose("client1", 1)).thenReturn(-1);
        when(p1.acceptRequest("client1", 1)).thenReturn(-1);
        
        MasterProposer c1 = new MasterProposer(
                "client1",
                paxosUrls(),
                connections);
        assertFalse(c1.propose(1));
    }
}
