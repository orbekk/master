package com.orbekk.same;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class MasterTest {
    private State state = new State("TestNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private TestBroadcaster broadcaster = new TestBroadcaster();
    private Master master;
    private MasterService masterS;

    public static class UnreachableClient implements ClientService {
        @Override
        public void setState(String component, String data, long revision)
                throws Exception {
            throw new Exception("Unreachable client");   
        }

        @Override
        public void masterTakeover(String masterUrl, String networkName,
                int masterId, String masterLocation)
                throws Exception {
            throw new Exception("Unreachable client");
        }

        @Override
        public void masterDown(int masterId) throws Exception {
            throw new Exception("Unreachable client");
        }
    }
    
    @Before
    public void setUp() {
        String masterLocation = "master:1000";
        state.update(".masterUrl", "http://master/MasterService.json", 1);
        state.update(".masterLocation", masterLocation, 1);
        master = new Master(state, connections, broadcaster,
                "http://master/MasterService.json", masterLocation);
        masterS = master.getService();
        connections.masterMap.put("http://master/MasterService.json",
                masterS);
        connections.masterMap0.put("master:1000", master.getNewService());
    }

    @Test
    public void joinNetworkAddsClient() throws Exception {
        masterS.joinNetworkRequest("http://clientUrl");
        List<String> participants = state.getList(".participants");
        assertTrue(participants.contains("http://clientUrl"));
    }

    @Test
    public void clientJoin() throws Exception {
        Client client = new Client(
                new State("ClientNetwork"), connections,
                "http://client/ClientService.json", "clientLocation", null);
        ClientService clientS = client.getService();
        connections.clientMap.put("http://client/ClientService.json", clientS);
        client.joinNetwork(master.getMasterInfo());
        master.performWork();
        assertTrue(state.getList(".participants").contains("http://client/ClientService.json"));
        assertEquals(state, client.testGetState());
    }

    @Test
    public void updateStateRequest() throws Exception {
        Client client1 = new Client(
                new State("ClientNetwork"), connections,
                "http://client/ClientService.json", "clientLocation", null);
        ClientService client1S = client1.getService();
        connections.clientMap.put("http://client/ClientService.json", client1S);
        Client client2 = new Client(
                new State("ClientNetwork"), connections,
                "http://client2/ClientService.json", "clientLocation", null);
        ClientService client2S = client2.getService();
        connections.clientMap.put("http://client2/ClientService.json", client2S);
        
        client1.joinNetwork(master.getMasterInfo());
        client2.joinNetwork(master.getMasterInfo());
        
        master.performWork();
        assertTrue(state.getList(".participants").contains("http://client/ClientService.json"));
        assertTrue(state.getList(".participants").contains("http://client2/ClientService.json"));
        assertEquals(state, client1.testGetState());
        
        assertTrue(masterS.updateStateRequest("A", "1", 0));
        master.performWork();
        
        assertEquals(state, client1.testGetState());
        assertEquals(state, client2.testGetState());
        
        assertFalse(masterS.updateStateRequest("A", "2", 0));
        assertTrue(masterS.updateStateRequest("A", "3", 1));
        master.performWork();
        
        assertEquals(state, client1.testGetState());
        assertEquals(state, client2.testGetState());
    }

    @Test
    public void masterRemovesParticipant() throws Exception {
        Client client = new Client(
                new State("ClientNetwork"), connections,
                "http://client/ClientService.json", "clientLocation", null);
        ClientService clientS = client.getService();
        connections.clientMap.put("http://client/ClientService.json", clientS);
        client.joinNetwork(master.getMasterInfo());
        master.performWork();
        assertTrue(state.getList(".participants").contains("http://client/ClientService.json"));
        
        connections.clientMap.put("http://client/ClientService.json",
                new UnreachableClient());
        masterS.updateStateRequest("NewState", "NewStateData", 0);
        master.performWork();
        
        assertEquals("[]", state.getDataOf(".participants"));
    }

}
