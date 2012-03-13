package com.orbekk.same;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class VariableUpdaterTaskTest {
    Variable<String> v;
    VariableUpdaterTask<String> updater;
    
    @Before public void setUp() {
        v = mock(Variable.class);
        updater = new VariableUpdaterTask<String>(v);
    }
        
    @Test
    public void updatesValue() {
        updater.set("FirstValue");
        updater.performWork();
        verify(v).set("FirstValue");
    }

    @Test
    public void noUpdateIfNotSet() {
        updater.set("FirstValue");
        updater.performWork();
        reset(v);
        updater.performWork();
        verify(v, never()).set(anyString());
    }
    
    @Test
    public void noUpdateIfNotReady() {
        updater.set("FirstValue");
        updater.performWork();
        reset(v);
        updater.set("SecondValue");
        updater.performWork();
        verify(v, never()).set(anyString());
    }
    
    @Test
    public void updatesWhenReady() {
        updater.set("Value1");
        updater.performWork();
        reset(v);
        updater.valueChanged(null);
        updater.set("Value2");
        updater.performWork();
        verify(v).set("Value2");
    }
    
    @Test
    public void choosesLastUpdate() {
        updater.set("FirstValue");
        updater.set("SecondValue");
        updater.performWork();
        verify(v).set("SecondValue");
    }
}
