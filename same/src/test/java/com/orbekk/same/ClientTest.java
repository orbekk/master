package com.orbekk.same;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.orbekk.util.DelayedOperation;

public class ClientTest {
    private State state = new State("ClientNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private Client client = new Client(state, connections,
            "http://client/ClientService.json", "clientLocation");
    private ClientService clientS = client.getService();
    private MasterService mockMaster = mock(MasterService.class);    
    
    @Before public void setUp() {
        connections.masterMap.put("master", mockMaster);
    }
    
    @Test public void disconnectedFailsUpdate() throws Exception {
        ClientInterface clientI = client.getInterface();
        DelayedOperation op = clientI.set(null);
        assertTrue(op.isDone());
        assertFalse(op.getStatus().isOk());
    }
    
    // TODO: Fix this test with protobuf rpc.
    @Ignore
    @Test public void connectedUpdateWorks() throws Exception {
        clientS.masterTakeover("master", "MyNetwork", 1, "master");
        ClientInterface clientI = client.getInterface();
        State.Component component = new State.Component(
                "TestVariable", 1, "meow");
        when(mockMaster.updateStateRequest("TestVariable", "meow", 1))
                .thenReturn(true);
        DelayedOperation op = clientI.set(component);
        assertTrue(op.getStatus().isOk());
    }
    
    @Test public void testSetState() throws Exception {
        clientS.setState("TestState", "Test data", 100);
        assertEquals(100, state.getRevision("TestState"));
        assertEquals("Test data", state.getDataOf("TestState"));
    }
    
    @Test public void stateListenerReceivesUpdate() throws Exception {
        StateChangedListener listener = mock(StateChangedListener.class);
        client.getInterface().addStateListener(listener);
        clientS.setState("StateListenerVariable", "100", 1);
        State.Component component = state.getComponent("StateListenerVariable");
        assertEquals("100", component.getData());
        verify(listener).stateChanged(eq(component));
    }
}
