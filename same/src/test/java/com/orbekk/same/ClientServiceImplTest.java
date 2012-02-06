package com.orbekk.same;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class ClientServiceImplTest {
    private State state = new State("ClientNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private Client client = new Client(state, connections,
            "http://client/ClientService.json");
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
        client.discover("mockClient/ClientService.json");
        verify(mockClient).notifyNetwork("ClientNetwork", "master");
    }
}
