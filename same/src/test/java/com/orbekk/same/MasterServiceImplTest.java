package com.orbekk.same;

import static org.junit.Assert.*;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.junit.Before;

public class MasterServiceImplTest {
    private State state = new State("TestNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private TestBroadcaster broadcaster = new TestBroadcaster();
    private MasterServiceImpl master = MasterServiceImpl.create(
            connections, broadcaster, "http://master/MasterService.json");

    public static class UnreachableClient implements ClientService {
        @Override
        public void notifyNetwork(String networkName, String masterUrl)
                throws Exception {
            throw new Exception("Unreachable client");
        }

        @Override
        public void setState(String component, String data, long revision)
                throws Exception {
            throw new Exception("Unreachable client");   
        }

        @Override
        public void discoveryRequest(String remoteUrl) throws Exception {
            throw new Exception("Unreachable client");
        }
    }
    
    @Before
    public void setUp() {
        connections.masterMap.put("http://master/MasterService.json", master);
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
        master.joinNetworkRequest("http://clientUrl");
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
        ClientServiceImpl client = new ClientServiceImpl(
                new State("ClientNetwork"), connections,
                "http://client/ClientService.json");
        ClientService clientS = client.getService();
        connections.clientMap.put("http://client/ClientService.json", clientS);
        client.joinNetwork("http://master");
        assertTrue(master._performWork());
        assertTrue(state.getList(".participants").contains("http://client/ClientService.json"));
        assertEquals(state, client.testGetState());
    }
    
    @Test
    public void validStateRequest() {
        ClientServiceImpl client1 = new ClientServiceImpl(
                new State("ClientNetwork"), connections,
                "http://client/ClientService.json");
        ClientService client1S = client1.getService();
        connections.clientMap.put("http://client/ClientService.json", client1S);
        ClientServiceImpl client2 = new ClientServiceImpl(
                new State("ClientNetwork"), connections,
                "http://client2/ClientService.json");
        ClientService client2S = client2.getService();
        connections.clientMap.put("http://client2/ClientService.json", client2S);
        
        client1.joinNetwork("http://master/MasterService.json");
        client2.joinNetwork("http://master/MasterService.json");
        
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
    
    @Test
    public void masterRemovesParticipant() {
        ClientServiceImpl client = new ClientServiceImpl(
                new State("ClientNetwork"), connections,
                "http://client/ClientService.json");
        ClientService clientS = client.getService();
        connections.clientMap.put("http://client/ClientService.json", clientS);
        client.joinNetwork("http://master");
        assertTrue(master._performWork());
        assertTrue(state.getList(".participants").contains("http://client/ClientService.json"));
        
        connections.clientMap.put("http://client/ClientService.json",
                new UnreachableClient());
        master.updateStateRequest("NewState", "NewStateData", 0);
        master._performWork();
        
        assertEquals("[]", state.getDataOf(".participants"));
    }
}
