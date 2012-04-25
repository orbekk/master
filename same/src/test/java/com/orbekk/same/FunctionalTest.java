package com.orbekk.same;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.orbekk.paxos.PaxosServiceImpl;
import com.orbekk.protobuf.Rpc;
import com.orbekk.util.DelayedOperation;

/** A functional test that runs with a master and several clients. */
public class FunctionalTest {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Master master;
    String masterUrl = "http://master/MasterService.json";
    String masterLocation = "master:1";
    Client client1;
    Client client2;
    Client client3;
    VariableFactory vf1;
    VariableFactory vf2;
    VariableFactory vf3;
    List<Client> clients = new ArrayList<Client>();
    TestConnectionManager connections = new TestConnectionManager();
    RpcFactory rpcf = new RpcFactory(5000) {
        @Override public Rpc create() {
            Rpc rpc = super.create();
            rpc.complete();
            return rpc;
        };
    };
    
    /** Works with a single thread executor. */ 
    public void awaitExecution() throws InterruptedException {
        final CountDownLatch finished = new CountDownLatch(1);
        Runnable sendFinished = new Runnable() {
            @Override public void run() {
                finished.countDown();
            }
        };
        executor.execute(sendFinished);
        finished.await();
    }
    
    @Before public void setUp() {
        master = Master.create(connections,
                masterUrl, "TestMaster", masterLocation, rpcf);
        connections.masterMap0.put(masterLocation, master.getNewService());
        client1 = newClient("TestClient1", "http://client1/ClientService.json",
                "client1");
        vf1 = new VariableFactory(client1.getInterface());
        client2 = newClient("TestClient2", "http://client2/ClientService.json",
                "client2");
        vf2 = new VariableFactory(client2.getInterface());
        client3 = newClient("TestClient3", "http://client3/ClientService.json",
                "client3");
        vf3 = new VariableFactory(client3.getInterface());
    }
    
    Client newClient(String clientName, String clientUrl, String location) {
        Client client = new Client(new State(clientName), connections,
                clientUrl, location, rpcf, executor);
        connections.clientMap0.put(location, client.getNewService());
        clients.add(client);
        String paxosUrl = clientUrl.replace("ClientService", "PaxosService");
        PaxosServiceImpl paxos = new PaxosServiceImpl(paxosUrl);
        connections.paxosMap0.put(location, paxos.getService());
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
           c.joinNetwork(master.getMasterInfo());
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
            List<String> participants = s.getList(State.PARTICIPANTS);
            assertThat(participants, hasItem("client1"));
            assertThat(participants, hasItem("client2"));
            assertThat(participants, hasItem("client3"));
        }
        for (Client c : clients) {
            assertThat(c.getConnectionState(), is(ConnectionState.STABLE));
            assertThat(c.getMaster().getMasterUrl(), is(masterUrl));
            assertThat(c.getMaster().getMasterLocation(), is(masterLocation));
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
    
    @Test public void clientBecomesMaster() throws Exception {
        String newMasterUrl = "http://newMaster/MasterService.json";
        String newMasterLocation = "newMaster:1";
        final Master newMaster = Master.create(connections,
                newMasterUrl, "TestMaster", newMasterLocation, rpcf);
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
        client1.startMasterElection(master.getMasterInfo());
        awaitExecution();
        newMaster.performWork();
        assertThat(client1.getMaster().getMasterLocation(), is(newMasterLocation));
        assertThat(client2.getMaster().getMasterLocation(), is(newMasterLocation));
    }
    
    @Test public void onlyOneNewMaster() throws Exception {
        String newMasterUrl = "http://newMaster/MasterService.json";
        String newMasterLocation = "newMaster:1";
        final Master newMaster = Master.create(connections,
                newMasterUrl, "TestMaster", newMasterLocation, rpcf);
        joinClients();
        MasterController controller = new MasterController() {
            boolean firstMaster = true;
            @Override
            public synchronized void enableMaster(State lastKnownState,
                    int masterId) {
                assertThat(firstMaster, is(true));
                newMaster.resumeFrom(lastKnownState, masterId);
                firstMaster = false;
            }
            @Override
            public void disableMaster() {
            }
        };
        client1.setMasterController(controller);
        client2.setMasterController(controller);
        client3.setMasterController(controller);
        client1.startMasterElection(master.getMasterInfo());
        awaitExecution();
        newMaster.performWork();
        assertThat(client1.getMaster().getMasterUrl(), is(newMasterUrl));
        assertThat(client2.getMaster().getMasterUrl(), is(newMasterUrl));
    }
    
    @Test public void masterFails() throws Exception {
        String newMasterUrl = "http://newMaster/MasterService.json";
        String newMasterLocation = "newMaster:2";
        final Master newMaster = Master.create(connections,
                newMasterUrl, "TestMaster", newMasterLocation, rpcf);
        joinClients();
        MasterController controller = new MasterController() {
            @Override
            public synchronized void enableMaster(State lastKnownState,
                    int masterId) {
                newMaster.resumeFrom(lastKnownState, masterId);
            }
            @Override
            public void disableMaster() {
            }
        };
        client1.setMasterController(controller);
        client2.setMasterController(controller);
        client3.setMasterController(controller);
        Variable<String> x1 = vf1.createString("TestMasterFailure");
        connections.masterMap0.put(masterLocation, null);
        assertThat(x1.set("Woop, woop").getStatus().getStatusCode(),
                is(DelayedOperation.Status.ERROR));
        awaitExecution();
        performWork();
        newMaster.performWork();
        assertThat(client1.getMaster().getMasterUrl(), is(newMasterUrl));
        assertThat(client2.getMaster().getMasterUrl(), is(newMasterUrl));
    }
}
