package com.orbekk.same;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.orbekk.paxos.PaxosService;
import com.orbekk.paxos.PaxosServiceImpl;

/** A functional test that runs with a master and several clients. */
public class FunctionalTest {
    Master master;
    String masterUrl = "http://master/MasterService.json";
    Client client1;
    Client client2;
    Client client3;
    VariableFactory vf1;
    VariableFactory vf2;
    VariableFactory vf3;
    List<Client> clients = new ArrayList<Client>();
    TestConnectionManager connections = new TestConnectionManager();
    TestBroadcaster broadcaster = new TestBroadcaster();
    
    @Before public void setUp() {
        master = Master.create(connections,
                broadcaster, masterUrl, "TestMaster");
        connections.masterMap.put(masterUrl,
                master.getService());
        client1 = newClient("TestClient1", "http://client1/ClientService.json");
        vf1 = new VariableFactory(client1.getInterface());
        client2 = newClient("TestClient2", "http://client2/ClientService.json");
        vf2 = new VariableFactory(client2.getInterface());
        client3 = newClient("TestClient3", "http://client3/ClientService.json");
        vf3 = new VariableFactory(client3.getInterface());
    }
    
    Client newClient(String clientName, String clientUrl) {
        Client client = new Client(new State(clientName), connections,
                clientUrl, broadcaster);
        connections.clientMap.put(clientUrl, client.getService());
        clients.add(client);
        String paxosUrl = clientUrl.replace("ClientService", "PaxosService");
        PaxosService paxos = new PaxosServiceImpl(paxosUrl);
        connections.paxosMap.put(paxosUrl, paxos);
        return client;
    }
    
    void performWork() {
        for (int i = 0; i < 2; i++) {
            master.performWork();
            for (Client c : clients) {
                c.performWork();
            }
        }
    }
    
    void joinClients() {
       for (Client c : clients) {
           c.joinNetwork(masterUrl);
       }
       performWork();
    }
    
    List<State> getStates() {
        List<State> states = new ArrayList<State>();
        states.add(master.state);
        for (Client c : clients) {
            states.add(c.state);
        }
        return states;
    }
    
    @Test public void testJoin() {
        joinClients();
        for (State s : getStates()) {
            List<String> participants = s.getList(".participants");
            assertThat(participants, hasItem("http://client1/ClientService.json"));
            assertThat(participants, hasItem("http://client2/ClientService.json"));
            assertThat(participants, hasItem("http://client3/ClientService.json"));
        }
        for (Client c : clients) {
            assertThat(c.getConnectionState(), is(ConnectionState.STABLE));
            assertThat(c.masterUrl, is(masterUrl));
        }
    }
    
    @Test public void setState() {
        joinClients();
        Variable<String> x1 = vf1.createString("x");
        Variable<String> x2 = vf2.createString("x");
        x1.set("TestValue1");
        performWork();
        x1.update();
        x2.update();
        assertThat(x1.get(), is("TestValue1"));
        assertThat(x2.get(), is("TestValue1"));
    }
    
    @Test public void clientBecomesMaster() {
        String newMasterUrl = "http://newMaster/MasterService.json";
        final Master newMaster = Master.create(connections,
                broadcaster, newMasterUrl, "TestMaster");
        connections.masterMap.put(newMasterUrl, newMaster.getService());
        joinClients();
        MasterController controller = new MasterController() {
            @Override
            public void enableMaster(State lastKnownState, int masterId) {
                newMaster.resumeFrom(lastKnownState, masterId);
            }
            @Override
            public void disableMaster() {
            }
        };
        client1.setMasterController(controller);
        client2.setMasterController(controller);
        client3.setMasterController(controller);
        client1.startMasterElection();
        newMaster.performWork();
        assertThat(client1.masterUrl, is(newMasterUrl));
        assertThat(client2.masterUrl, is(newMasterUrl));
    }
}
