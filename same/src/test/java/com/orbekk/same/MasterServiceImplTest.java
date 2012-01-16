package com.orbekk.same;

import static org.junit.Assert.*;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.junit.Before;

public class MasterServiceImplTest {
    private State state = new State("TestNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private TestBroadcaster broadcaster = new TestBroadcaster(connections);
    private MasterServiceImpl master = new MasterServiceImpl(state,
            connections, broadcaster);
        
    @Test
    public void setsMasterUrl() {
        master.setUrl("http://10.0.0.54:10050/");
        assertEquals("http://10.0.0.54:10050/MasterService.json",
                state.getDataOf(".masterUrl"));
    }
    
    @Test
    public void testJsonState() {
        List<String> participants =
                state.getParsedData(".participants",
                        new TypeReference<List<String>>() { });
        assertEquals(participants.size(), 0);
        participants.add("http://SomeUrl/");
        state.updateFromObject(".participants", participants, 1);
    }
    
    @Test
    public void joinNetworkAddsClient() {
        master.joinNetworkRequest("TestNetwork", "http://clientUrl");
        List<String> participants = state.getList(".participants");
        assertTrue(participants.contains("http://clientUrl"));
    }
    
    @Test
    public void workLoopClearsUpdatedComponents() {
        state.update("Test", "Content", 0);
        assertTrue(master._performWork());
        assertTrue(state.getAndClearUpdatedComponents().isEmpty());
    }

    @Test
    public void clientJoin() {
        master.setUrl("http://master/");
        ClientServiceImpl client = new ClientServiceImpl(
                new State("ClientNetwork"), connections);
        client.setUrl("http://client/");
        connections.clientMap.put("http://client/ClientService.json", client);
        master.joinNetworkRequest("TestNetwork", "http://client/ClientService.json");
        assertTrue(master._performWork());
        assertTrue(state.getList(".participants").contains("http://client/ClientService.json"));
        assertEquals(state, client.testGetState());
    }
    
    @Test
    public void validStateRequest() {
        master.setUrl("http://master/");
        ClientServiceImpl client1 = new ClientServiceImpl(
                new State("ClientNetwork"), connections);
        client1.setUrl("http://client/");
        connections.clientMap.put("http://client/ClientService.json", client1);
        ClientServiceImpl client2 = new ClientServiceImpl(
                new State("ClientNetwork"), connections);
        client1.setUrl("http://client2/");
        connections.clientMap.put("http://client2/ClientService.json", client2);
        
        master.joinNetworkRequest("TestNetwork", "http://client/ClientService.json");
        master.joinNetworkRequest("TestNetwork", "http://client2/ClientService.json");
        
        assertTrue(master._performWork());
        assertTrue(state.getList(".participants").contains("http://client/ClientService.json"));
        assertTrue(state.getList(".participants").contains("http://client2/ClientService.json"));
        assertEquals(state, client1.testGetState());
        
        assertTrue(master.updateStateRequest("A", "1", 0));
        assertTrue(master._performWork());
        
        assertEquals(state, client1.testGetState());
        assertEquals(state, client2.testGetState());
        
        assertFalse(master.updateStateRequest("A", "2", 0));
        assertTrue(master.updateStateRequest("A", "3", 1));
        assertTrue(master._performWork());
        
        assertEquals(state, client1.testGetState());
        assertEquals(state, client2.testGetState());
    }
}
