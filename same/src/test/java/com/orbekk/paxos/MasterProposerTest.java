package com.orbekk.paxos;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.orbekk.same.TestConnectionManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class MasterProposerTest {
    TestConnectionManager connections = new TestConnectionManager();
    PaxosService p1 = mock(PaxosService.class);
    PaxosService p2 = mock(PaxosService.class);
    PaxosService p3 = mock(PaxosService.class);
    PaxosService p4 = mock(PaxosService.class);
    PaxosService p5 = mock(PaxosService.class);
    String master = null;
    
    private class TestMasterAction implements Runnable {
        String tag;
        TestMasterAction(String tag) {
            this.tag = tag;
        }
        
        @Override public void run() {
            master = tag;
        }
    }
    
    @Before public void setUp() {
    }
    
    List<String> paxosUrls() {
        List<String> urls = new ArrayList<String>();
        urls.addAll(connections.paxosMap.keySet());
        return urls;
    }
    
    @Test public void successfulProposal() {
        connections.paxosMap.put("p1", p1);
        when(p1.propose("client1", 1, 1)).thenReturn(true);
        when(p1.acceptRequest("client1", 1, 1)).thenReturn(true);
        
        MasterProposer c1 = new MasterProposer(
                "client1",
                paxosUrls(),
                0,
                connections,
                MasterProposer.getTimeoutAction(0),
                new TestMasterAction("c1"));
        c1.run();
        assertEquals("c1", master);
    }
}
