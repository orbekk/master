package com.orbekk.same;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.HashMap;
import org.junit.Test;
import org.junit.Before;

public class SameStateTest {
    private MockConnectionManager connections;
    private SameState state1, state2, state3;
    private SameService service1, service2, service3;

    public static class MockConnectionManager implements ConnectionManager {
        public Map<String, SameService> connections =
                new HashMap<String, SameService>();

        @Override
        public SameService getConnection(String url) {
            return connections.get(url);
        }
    }

    public SameStateTest() {
    }

    @Before public void setUp() {
        connections = new MockConnectionManager();

        state1 = new SameState("Network1", "Client1", connections);
        state1.setUrl("test://client1");
        service1 = new SameServiceImpl(state1);
        state2 = new SameState("Network2", "Client2", connections);
        state2.setUrl("test://client2");
        service2 = new SameServiceImpl(state2);
        state3 = new SameState("Network3", "Client3", connections);
        state3.setUrl("test://client3");
        service3 = new SameServiceImpl(state3);

        connections.connections.put(state1.getUrl(), service1);
        connections.connections.put(state2.getUrl(), service2);
        connections.connections.put(state3.getUrl(), service3);
    }

    @Test public void testJoinNetwork() {
        connections.getConnection(state1.getUrl()).
            participateNetwork("Network1", state2.getClientId(),
                    state2.getUrl());
        assertTrue(state1.getParticipants().size() == 1);
        assertTrue(state2.getParticipants().size() == 1);

        state1.internalRun();
        state2.internalRun();

        assertTrue(state1.getParticipants().size() == 2);
        assertTrue(state2.getParticipants().size() == 2);
        assertEquals(state1.getNetworkName(), state2.getNetworkName());
    }
}
