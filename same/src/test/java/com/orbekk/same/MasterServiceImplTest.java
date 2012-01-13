package com.orbekk.same;

import static org.junit.Assert.*;

import org.junit.Test;

public class MasterServiceImplTest {
    private State state = new State();
    private MasterServiceImpl master = new MasterServiceImpl(state);
        
    @Test
    public void setsMasterUrl() {
        master.setUrl("http://10.0.0.54:10050/");
        assertEquals("http://10.0.0.54:10050/MasterService.json",
                state.getDataOf(".masterUrl"));
    }
}
