package com.orbekk.same;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class ClientServiceImplTest {
    private State state = new State("ClientNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private TestBroadcaster broadcaster = new TestBroadcaster();
    private ClientServiceImpl client = new ClientServiceImpl(state, connections);
    private ClientService clientS = client.getService();
    
    @Test public void testSetState() throws Exception {
        clientS.setState("TestState", "Test data", 100);
        assertEquals(100, state.getRevision("TestState"));
        assertEquals("Test data", state.getDataOf("TestState"));
    }
    
    @Test public void testNetworkListener() throws Exception {
        NetworkNotificationListener listener =
                mock(NetworkNotificationListener.class);
        client.setNetworkListener(listener);
        clientS.notifyNetwork("MyNetwork", "MasterUrl");
        verify(listener).notifyNetwork("MyNetwork", "MasterUrl");
    }
    
    @Test public void discover() throws Exception {
        clientS.setState(".masterUrl", "master", 1);
        ClientService mockClient = mock(ClientService.class);
        connections.clientMap.put("mockClient/ClientService.json",
                mockClient);
        client.discover("mockClient/");
        verify(mockClient).notifyNetwork("ClientNetwork", "master");
    }
}
