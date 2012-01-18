package com.orbekk.same;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClientServiceImplTest {
    private State state = new State("ClientNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private TestBroadcaster broadcaster = new TestBroadcaster();
    private ClientServiceImpl client = new ClientServiceImpl(state, connections);
    
    @Test public void testSetState() {
        client.setState("TestState", "Test data", 100);
        assertEquals(100, state.getRevision("TestState"));
        assertEquals("Test data", state.getDataOf("TestState"));
    }
}
