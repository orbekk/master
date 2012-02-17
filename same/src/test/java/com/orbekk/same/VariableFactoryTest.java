package com.orbekk.same;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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
    TypeReference<String> stringType = new TypeReference<String>() {};
    
    @Before
    public void setUp() {
        client = mock(Client.ClientInterface.class);
        vf = new VariableFactory(client);
        initializeSampleState();
        when(client.getState()).thenReturn(sampleState);
    }
    
    public void initializeSampleState() {
        sampleState = new State("TestState");
        sampleState.update("TestVariable", "1", 1);
        sampleState.update("TestList", "[]", 1);
    }
    
    @Test
    public void getsInitialValue() {
        Variable<Integer> testVariable = vf.create("TestVariable", intType);
        assertEquals(1, (int)testVariable.get());
    }
    
    @Test
    public void updatesValue() {
        Variable<List<String>> list = vf.create("TestList", listType);
        assertTrue(list.get().isEmpty());
        sampleState.update("TestList", "[\"CONTENT\"]", 2);
        list.update();
        assertEquals(1, list.get().size());
        assertEquals("CONTENT", list.get().get(0));
    }
    
    @Test
    public void setsValue() throws Exception {
        Variable<String> string = vf.create("X", stringType);
        assertNull(string.get());
        string.set("NewValue");
        verify(client).set("X", "\"NewValue\"", 0);
    }
}
