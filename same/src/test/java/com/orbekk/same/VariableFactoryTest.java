package com.orbekk.same;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

public class VariableFactoryTest {
    Client.ClientInterface client;
    VariableFactory vf;
    State sampleState;
    
    TypeReference<Integer> intType = new TypeReference<Integer>() {};
    TypeReference<List<String>> listType = new TypeReference<List<String>>() {};
    
    @Before
    public void setUp() {
        client = mock(Client.ClientInterface.class);
        vf = new VariableFactory(client);
        initializeSampleState();
    }
    
    public void initializeSampleState() {
        sampleState = new State("TestState");
        sampleState.update("TestVariable", "1", 1);
        sampleState.update("TestList", "[]", 1);
    }
    
    @Test
    public void getsInitialValue() {
        when(client.getState()).thenReturn(sampleState);
        Variable<Integer> testVariable = vf.create("TestVariable", intType);
        assertEquals(1, (int)testVariable.get());
    }
    
    @Test
    public void updatesValue() {
        when(client.getState()).thenReturn(sampleState);
        Variable<List<String>> list = vf.create("TestList", listType);
        assertTrue(list.get().isEmpty());
        sampleState.update("TestList", "[\"CONTENT\"]", 2);
        list.update();
        assertEquals(1, list.get().size());
        assertEquals("CONTENT", list.get().get(0));
    }
}
