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
        master.setUrl("http://master");
        ClientServiceImpl client = new ClientServiceImpl(
                new State("ClientNetwork"), connections);
        client.setUrl("http://client");
        connections.clientMap.put("http://client", client);
        master.joinNetworkRequest("TestNetwork", "http://client");
        assertTrue(master._performWork());
        assertTrue(state.getList(".participants").contains("http://client"));
        assertEquals(state, client.testGetState());
    }
}
