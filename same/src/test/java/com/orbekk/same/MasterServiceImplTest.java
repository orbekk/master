package com.orbekk.same;

import static org.junit.Assert.*;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

public class MasterServiceImplTest {
    private State state = new State("TestNetwork");
    private MasterServiceImpl master = new MasterServiceImpl(state);
        
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
}
