package com.orbekk.same;

import static org.junit.Assert.*;

import java.util.List;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

public class NewMasterTest {
    private State state = new State("TestNetwork");
    private TestConnectionManager connections = new TestConnectionManager();
    private TestBroadcaster broadcaster = new TestBroadcaster();
    private NewMaster master;
    private MasterService masterS;

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
        state.update(".masterUrl", "http://master/MasterService.json", 1);
        master = new NewMaster(state, connections, broadcaster);
        masterS = master.getService();
        connections.masterMap.put("http://master/MasterService.json",
                masterS);
    }

    @Test
    public void updateStateRequest() throws Exception {
        Client client1 = new Client(
                new State("ClientNetwork"), connections,
                "http://client/ClientService.json");
        ClientService client1S = client1.getService();
        connections.clientMap.put("http://client/ClientService.json", client1S);
        Client client2 = new Client(
                new State("ClientNetwork"), connections,
                "http://client2/ClientService.json");
        ClientService client2S = client2.getService();
        connections.clientMap.put("http://client2/ClientService.json", client2S);
        
        client1.joinNetwork("http://master/MasterService.json");
        client2.joinNetwork("http://master/MasterService.json");
        
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
}
